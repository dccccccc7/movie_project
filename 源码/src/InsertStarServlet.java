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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;


@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insert-star")
public class InsertStarServlet extends HttpServlet {
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

        String name = request.getParameter("star-name");
        String year = request.getParameter("year");
        if (year.length() == 0) {
            year = null;
        }

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            String insertStar = "CALL insert_star( ?, ?);";
            // Declare our statement
            PreparedStatement insertStatement = dbcon.prepareStatement(insertStar);
            insertStatement.setString(1, name);
            insertStatement.setString(2, year);
            // Perform the insertion
            insertStatement.executeUpdate();

            String starIdQuery = "SELECT MAX(id) FROM stars;";
            // Declare our statement
            PreparedStatement starIdStatement = dbcon.prepareStatement(starIdQuery);
            // Perform the query
            ResultSet newStarId = starIdStatement.executeQuery(starIdQuery);
            // Get the newly generated star ID
            newStarId.next();
            String starId = newStarId.getString(1);


            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", starId);

            response.getWriter().write(responseJsonObject.toString());

            // set response status to 200 (OK)
            starIdStatement.close();
            newStarId.close();
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
