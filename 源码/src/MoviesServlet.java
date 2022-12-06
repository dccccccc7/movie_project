import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String history = null;
        if (request.getParameter("history") != null) history = request.getParameter("history");
        if (history != null) {
            if (history.equalsIgnoreCase("true")) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("previousQuery", session.getAttribute("previousQuery").toString());
                out.write(jsonObject.toString());
                return;
            } else {
                session.setAttribute("previousQuery", request.getQueryString());
            }
        } else {
            session.setAttribute("previousQuery", request.getQueryString());
        }

        // initialize out here to debug
        String movieListQuery = "";
        // Initialize null parameters:
        String type = null;
        String title = null;
        String year = null;
        String director = null;
        String star = null;
        String genreId = null;
        String numResults = null;
        String page = null;
        String sortBy = null;
//        String sortOrder = null;

        // Get search parameters:
        if (request.getParameter("type") != null) type = request.getParameter("type");
        if (request.getParameter("title") != null) title = request.getParameter("title");
        if (request.getParameter("year") != null) year = request.getParameter("year");
        if (request.getParameter("director") != null) director = request.getParameter("director");
        if (request.getParameter("star") != null) star = request.getParameter("star");
        if (request.getParameter("genreId") != null) genreId = request.getParameter("genreId");
        if (request.getParameter("sortBy") != null) sortBy = request.getParameter("sortBy");
//        if (request.getParameter("sortOrder") != null) sortOrder = request.getParameter("sortOrder");
        numResults = (request.getParameter("numResults") != null) ? request.getParameter("numResults") : "10";
        page = (request.getParameter("page") != null) ? request.getParameter("page") : "1";

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement

            Integer offset = (Integer.parseInt(numResults) * (Integer.parseInt(page) - 1));

            String havingClause = " ";
                if (genreId != null) {
                    havingClause += "HAVING genreIds REGEXP '\\\\b" + genreId + "\\\\b' ";
                }
            String whereQuery = " ";
            if (year != null) {
                whereQuery += "AND m.year = " + year + " ";
            }

            String likeQuery = " ";
            if (type != null && title != null) {
                if (type.equals("browse")) {
                    likeQuery = "AND m.title ";
                    if (title.equals("*")) {
                        likeQuery += "REGEXP '^[^a-zA-Z0-9\\s]'  ";
                    } else {
                        likeQuery += "LIKE '" + title + "%' ";
                    }
                }
            }

            String starsQueryTable = " ";
            if (star != null) {
                starsQueryTable = ",stars s, stars_in_movies sim ";
                whereQuery += "AND s.id = sim.starId AND sim.movieId = m.id";
            }

            if (type != null) {
                if (type.equals("search")) {
                    if (title != null) {
                        likeQuery += "AND m.title LIKE '%" + title + "%' ";
                    }
                    if (director != null) {
                        likeQuery += "AND m.director LIKE '%" + director + "%' ";
                    }
                    if (star != null) {
                        likeQuery += "AND s.name LIKE '%" + star + "%' ";
                    }
                } else if (type.equals("quickSearch")) {
                    if (title != null) {
                        likeQuery += "AND MATCH (title) AGAINST ('" + title + "') ";
                    }
                }
            }

            String orderByAtrQuery = "r.rating DESC, m.title DESC";
            if (sortBy != null) {
                orderByAtrQuery = sortBy;
            }
//            if (sortBy != null && sortOrder != null) {
//                if (sortBy.equalsIgnoreCase("title") && sortOrder.equalsIgnoreCase("asc")) {
//                    orderByAtrQuery = "m.title " + "ASC" + ", r.rating " + "ASC";
//                } else if (sortBy.equalsIgnoreCase("title") && sortOrder.equalsIgnoreCase("desc")) {
//                    orderByAtrQuery = "m.title " + "DESC" + ", r.rating " + "DESC";
//                } else if (sortBy.equalsIgnoreCase("rating") && sortOrder.equalsIgnoreCase("asc")) {
//                    orderByAtrQuery = "r.rating " + "ASC" + ", m.title " + "ASC";
//                }
//            }

            movieListQuery =
                "SELECT m.id AS movieId, m.title, m.year, m.director, r.rating, \n " +
                "GROUP_CONCAT(DISTINCT g.id) AS genreIds, GROUP_CONCAT(DISTINCT g.name) AS genreNames\n " +
                "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, \n" +
                "genres g, genres_in_movies gim " + starsQueryTable + "\n" +
                "WHERE g.id = gim.genreId AND m.id = gim.movieId" + whereQuery + likeQuery +
                "GROUP BY m.id " + havingClause + "\n" +
                "ORDER BY " + orderByAtrQuery + "\n" +
                "LIMIT " + (Integer.parseInt(numResults) + 1) + " OFFSET " + offset + ";";
            // Perform the query
            PreparedStatement statement = dbcon.prepareStatement(movieListQuery);

            ResultSet movieListResults = statement.executeQuery();

            JsonArray moviesArray = new JsonArray();

            // Iterate through each row of rs
            while (movieListResults.next()) {
                String movie_id = movieListResults.getString("movieId");
                String movie_title = movieListResults.getString("title");
                String movie_year = movieListResults.getString("year");
                String movie_director = movieListResults.getString("director");
                String movie_rating = movieListResults.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject movieObject = new JsonObject();
                movieObject.addProperty("movie_id", movie_id);
                movieObject.addProperty("movie_title", movie_title);
                movieObject.addProperty("movie_year", movie_year);
                movieObject.addProperty("movie_director", movie_director);
                movieObject.addProperty("movie_rating", movie_rating);

                // Grab Genres from movieId
                String[] genresIdsArray = movieListResults.getString("genreIds").split(",");
                String[] genresNamesArray = movieListResults.getString("genreNames").split(",");
                JsonArray genres = new JsonArray();

                for (int i = 0; i < genresIdsArray.length; i++) {
                    try {
                        String genre_id = genresIdsArray[i];
                        String genre_name = genresNamesArray[i];

                        JsonObject genresObject = new JsonObject();
                        genresObject.addProperty("genre_id", genre_id);
                        genresObject.addProperty("genre_name", genre_name);

                        genres.add(genresObject);
                    } catch (Exception ex) {}
                }

                // Grab 3 Stars from movieId
                String movieStarsQuery =
                        "SELECT s.id AS starId, s.name\n" +
                        "FROM movies m, stars s, stars_in_movies sim\n" +
                        "WHERE sim.movieId = m.id AND sim.starId = s.id \n" +
                        "AND s.id IN \n" +
                        "(SELECT s.id FROM movies m, stars s, stars_in_movies sim\n" +
                        "WHERE sim.movieId = ? AND sim.starId = s.id)\n" +
                        "GROUP BY s.id\n" +
                        "ORDER BY COUNT(sim.movieId) DESC\n" +
                        "LIMIT 3;";

                PreparedStatement statement2 = dbcon.prepareStatement(movieStarsQuery);
                statement2.setString(1, movie_id);
                ResultSet movieStarsResult = statement2.executeQuery();
                JsonArray stars = new JsonArray();

                while (movieStarsResult.next()) {
                    String star_id = movieStarsResult.getString("starId");
                    String star_name = movieStarsResult.getString("name");

                    JsonObject starsObject = new JsonObject();
                    starsObject.addProperty("star_id", star_id);
                    starsObject.addProperty("star_name", star_name);

                    stars.add(starsObject);
                }

                movieStarsResult.close();

                // Finally add everything to movieArray
                movieObject.add("movie_genres", genres);
                movieObject.add("movie_stars", stars);

                moviesArray.add(movieObject);
            }

            // write JSON string to output
            out.write(moviesArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            movieListResults.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("SQL", movieListQuery);
            jsonObject.addProperty("line", e.getStackTrace()[0].getLineNumber());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();

    }
}
