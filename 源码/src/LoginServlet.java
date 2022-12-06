import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private static boolean verifyEmployeeCredentials(String email, String password) throws Exception {

        String loginUser = "root";
        String loginPasswd = "dongchen01";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        String query = String.format("SELECT * from employees where email='%s'", email);

        ResultSet rs = statement.executeQuery(query);

        boolean success = false;
        if (rs.next()) {
            // get the encrypted password from the database
            String encryptedPassword = rs.getString("password");

            // use the same encryptor to compare the user input password with encrypted password stored in DB
            success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
        }

        rs.close();
        statement.close();
        connection.close();

//        System.out.println("verify " + email + " - " + password);

        return success;
    }

    private static boolean verifyCredentials(String email, String password) throws Exception {

        String loginUser = "root";
        String loginPasswd = "dongchen01";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        String query = String.format("SELECT * from customers where email='%s'", email);

        ResultSet rs = statement.executeQuery(query);

        boolean success = false;
        if (rs.next()) {
            // get the encrypted password from the database
            String encryptedPassword = rs.getString("password");

            // use the same encryptor to compare the user input password with encrypted password stored in DB
            success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
        }

        rs.close();
        statement.close();
        connection.close();

        System.out.println("verify " + email + " - " + password);

        return success;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
//        try {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        } catch (Exception e) {
//            responseJsonObject.addProperty("status", "fail");
//            responseJsonObject.addProperty("message", "Please verify that you are not a robot.");
//            response.getWriter().write(responseJsonObject.toString());
//            // set response status to 200 (OK)
//            response.setStatus(200);
//            out.close();
//
//            return;
//        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String userId = "";
        boolean userLoginExists = false;

        try {

            // if employee, log in with employee credentials
            if (verifyEmployeeCredentials(username, password)) {
                // Login success:

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));

                // make this user have employee status
                User u = (User) request.getSession().getAttribute("user");
                u.setEmployeeStatus(true);

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                response.getWriter().write(responseJsonObject.toString());
                // set response status to 200 (OK)
                response.setStatus(200);
                out.close();

                return;
            }

            // check normal credentials
            if (verifyCredentials(username, password)) {
                // Get a connection from dataSource
                Connection dbcon = dataSource.getConnection();

                String getUserIdQuery =
                        "SELECT email, id \n" +
                                "FROM customers c \n" +
                                "WHERE c.email = ? \n" +
                                "LIMIT 1;";

                PreparedStatement statement = dbcon.prepareStatement(getUserIdQuery);
                statement.setString(1, username);

                // Perform the query
                ResultSet userIdFromEmail = statement.executeQuery();
                if (userIdFromEmail.next()) {
                    userLoginExists = true;
                    userId = userIdFromEmail.getString("id");
                }

                dbcon.close();
                userIdFromEmail.close();
                statement.close();
            }

            if (userLoginExists) {
                // Login success:

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));
                request.getSession().setAttribute("customerId", userId);

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid login credentials");
            }

            response.getWriter().write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

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
