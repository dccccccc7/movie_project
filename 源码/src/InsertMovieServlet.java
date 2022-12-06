import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;


@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert-movie")
public class InsertMovieServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();

        // get parameters
        String title = request.getParameter("movie-title");
        String year = request.getParameter("movie-year");
        String director = request.getParameter("movie-director");
        String star = request.getParameter("star-name");
        String genre = request.getParameter("genre");


        // check if movie exists. If it does, fail

        // check if genre exists

        // check if star exists

        // insert the movie using stored procedure

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // check if movie exists. If it does, fail
            String getMovieQuery =
                    "SELECT title, year, director \n" +
                            "FROM movies m \n" +
                            "WHERE m.title = ? AND m.year = ? AND m.director = ? \n" +
                            "LIMIT 1;";
            // Declare our statement
            PreparedStatement getMovieStatement = dbcon.prepareStatement(getMovieQuery);
            getMovieStatement.setString(1, title);
            getMovieStatement.setString(2, year);
            getMovieStatement.setString(3, director);
            // Perform the query
            ResultSet existingMovies = getMovieStatement.executeQuery();
            if (existingMovies.next()) {
                existingMovies.close();
                getMovieStatement.close();

                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Cannot add a movie that already exists.");
                responseJsonObject.addProperty("star", "Unable to add star in movie.");
                responseJsonObject.addProperty("genre", "Unable to add genre in movie.");

                response.getWriter().write(responseJsonObject.toString());
                // set response status to 200 (OK)
                response.setStatus(200);
                dbcon.close();
                out.close();
                return;
            }

            // movie does not exist yet, so insert!
            else {
                // check if star exists
                boolean starExists = false;
                String starId = "";
                String getStarQuery =
                        "SELECT id, name \n" +
                                "FROM stars m \n" +
                                "WHERE m.name = ? \n" +
                                "LIMIT 1;";
                // Declare our statement
                PreparedStatement getStarStatement = dbcon.prepareStatement(getStarQuery);
                getStarStatement.setString(1, star);
                // Perform the query
                ResultSet existingStar = getStarStatement.executeQuery();
                if (existingStar.next()) {
                    starExists = true;
                    starId = existingStar.getString(1);
                }
                getStarStatement.close();
                existingStar.close();


                // check if genre exists
                boolean genreExists = false;
                String genreId = "";
                String getGenreQuery =
                        "SELECT id, name \n" +
                                "FROM genres g \n" +
                                "WHERE g.name = ? \n" +
                                "LIMIT 1;";
                // Declare our statement
                PreparedStatement getGenreStatement = dbcon.prepareStatement(getGenreQuery);
                getGenreStatement.setString(1, genre);
                // Perform the query
                ResultSet existingGenre = getGenreStatement.executeQuery();
                if (existingGenre.next()) {
                    genreExists = true;
                    genreId = existingGenre.getString(1);
                }
                getGenreStatement.close();
                existingGenre.close();


                // insert movie
                String insertMovie = "CALL insert_movie( ?, ?, ?, ?, ?);";
                // Declare our statement
                PreparedStatement insertMovieStatement = dbcon.prepareStatement(insertMovie);
                insertMovieStatement.setString(1, title);
                insertMovieStatement.setString(2, year);
                insertMovieStatement.setString(3, director);
                insertMovieStatement.setString(4, star);
                insertMovieStatement.setString(5, genre);
                // Perform the insertion
                insertMovieStatement.executeUpdate();


                // get the inserted movie's id
                String movieIdQuery = "SELECT id FROM movies m WHERE m.title = ? AND m.year = ? AND m.director = ?;";
                // Declare our statement
                PreparedStatement movieIdStatement = dbcon.prepareStatement(movieIdQuery);
                movieIdStatement.setString(1, title);
                movieIdStatement.setString(2, year);
                movieIdStatement.setString(3, director);
                // Perform the query
                ResultSet newMovieId = movieIdStatement.executeQuery();
                // Get the newly generated star ID
                newMovieId.next();
                String movieId = newMovieId.getString(1);


                // get newly generated genre id if one was created
                if (!genreExists) {
                    String newGenreIdQuery = "SELECT MAX(id) FROM genres;";
                    // Declare our statement
                    PreparedStatement genreIdStatement = dbcon.prepareStatement(newGenreIdQuery);
                    // Perform the query
                    ResultSet newGenreId = genreIdStatement.executeQuery(newGenreIdQuery);
                    // Get the newly generated star ID
                    newGenreId.next();
                    genreId = newGenreId.getString(1);

                    genreIdStatement.close(); newGenreId.close();
                }


                // get newly generated star id if one was created
                if (!starExists) {
                    String newStarIdQuery = "SELECT MAX(id) FROM stars;";
                    // Declare our statement
                    PreparedStatement starIdStatement = dbcon.prepareStatement(newStarIdQuery);
                    // Perform the query
                    ResultSet newStarId = starIdStatement.executeQuery(newStarIdQuery);
                    // Get the newly generated star ID
                    newStarId.next();
                    starId = newStarId.getString(1);

                    starIdStatement.close(); newStarId.close();
                }


                String genreMessage = "Created new genre: ";
                String starMessage = "Created new star: ";
                if (genreExists)
                    genreMessage = "Used existing genre: ";
                if (starExists)
                    starMessage = "Used existing star: ";

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Movie successfully inserted. Movie ID: " + movieId);
                responseJsonObject.addProperty("star", starMessage + star + ". ID: " + starId);
                responseJsonObject.addProperty("genre", genreMessage + genre + ". ID: " + genreId);

                getStarStatement.close(); existingStar.close();
                getGenreStatement.close(); existingGenre.close();
            }
            getMovieStatement.close();
            existingMovies.close();


            response.getWriter().write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            dbcon.close();
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();
    }
}
