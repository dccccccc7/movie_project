import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String movieQuery =
                    "SELECT m.id AS movie_id, m.title, m.year, m.director, r.rating \n" +
                            "FROM movies m LEFT JOIN ratings r \n" +
                            "ON m.id = r.movieId\n" +
                            "WHERE m.id = ?;";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(movieQuery);

            String movieGenresQuery =
                    "SELECT g.id AS genreId, g.name\n" +
                            "FROM movies m, genres_in_movies gim, genres g\n" +
                            "WHERE gim.movieId = ?" +
                            "AND m.id = gim.movieId\n" +
                            "AND gim.genreId = g.id;";

            PreparedStatement statement2 = dbcon.prepareStatement(movieGenresQuery);

            String movieStarsQuery =
                    "SELECT s.id AS starId, s.name, s.birthYear, COUNT(sim.movieId) AS total_movies\n" +
                            "FROM movies m, stars s, stars_in_movies sim\n" +
                            "WHERE sim.movieId = m.id AND sim.starId = s.id \n" +
                            "AND s.id IN \n" +
                            "(SELECT s.id FROM movies m, stars s, stars_in_movies sim\n" +
                            "WHERE sim.movieId = ? AND sim.starId = s.id)\n" +
                            "GROUP BY s.id\n" +
                            "ORDER BY COUNT(sim.movieId) DESC;";

            PreparedStatement statement3 = dbcon.prepareStatement(movieStarsQuery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("movie_id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                // Grab All Genres from movieId
                statement2.setString(1, movie_id);
                ResultSet movieGenresResult = statement2.executeQuery();

                JsonArray genres = new JsonArray();
                while (movieGenresResult.next()) {
                    String genre_id = movieGenresResult.getString("genreId");
                    String genre_name = movieGenresResult.getString("name");

                    JsonObject genresObject = new JsonObject();
                    genresObject.addProperty("genre_id", genre_id);
                    genresObject.addProperty("genre_name", genre_name);

                    genres.add(genresObject);
                }
                movieGenresResult.close();

                // Grab All Stars from movieId
                statement3.setString(1, movie_id);
                ResultSet movieStarsResult = statement3.executeQuery();

                JsonArray stars = new JsonArray();
                while (movieStarsResult.next()) {
                    String star_id = movieStarsResult.getString("starId");
                    String star_name = movieStarsResult.getString("name");
                    String star_dob = movieStarsResult.getString("birthYear");
                    String star_total_movies = movieStarsResult.getString("total_movies");

                    JsonObject starsObject = new JsonObject();
                    starsObject.addProperty("star_id", star_id);
                    starsObject.addProperty("star_name", star_name);
                    starsObject.addProperty("star_dob", star_dob);
                    starsObject.addProperty("star_total_movies", star_total_movies);

                    stars.add(starsObject);
                }
                movieStarsResult.close();
                jsonObject.add("movie_genres", genres);
                jsonObject.add("movie_stars", stars);

                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

}
