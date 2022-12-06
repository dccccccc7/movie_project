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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.Month;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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
        HttpSession session = request.getSession();

        // get customerId from session
        String customerId = (String) session.getAttribute("customerId");
        // get shopping cart items from session
        HashMap<String, JsonObject> previousItems = (HashMap<String, JsonObject>) session.getAttribute("previousItems");
        // TODO: What if shopping cart is empty when they try to checkout?

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        int expirationMonth = Integer.parseInt(request.getParameter("expirationMonth"));
        String expirationYear = request.getParameter("expirationYear");

        boolean creditCardIsValid = false;
        boolean expiredCard = false;

        // Check if date has passed
        Date today = new Date();
        String expDateStr = String.format("01/%02d/%4d", expirationMonth, Integer.parseInt(expirationYear));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date expirationDate = null;
        try {
            expirationDate = formatter.parse(expDateStr);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        if (expirationDate != null && expirationDate.before(today)) {
            expiredCard = true;
        }

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            if (!expiredCard) {
                String creditCardQuery =
                        "SELECT *\n" +
                                "FROM creditcards c \n" +
                                "WHERE c.firstName = ? \n" +
                                "AND c.lastName = ? \n" +
                                "AND MONTH(c.expiration) = ? \n" +
                                "AND YEAR(c.expiration) = ? \n" +
                                "LIMIT 1;";

                // Declare our statement
                PreparedStatement statement = dbcon.prepareStatement(creditCardQuery);
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setInt(3, expirationMonth);
                statement.setString(4, expirationYear);

                // Perform the query
                ResultSet creditCardResult = statement.executeQuery();

                if (creditCardResult.next()) {
                    creditCardIsValid = true;
                }

                statement.close();
                creditCardResult.close();
            }

            JsonObject responseJsonObject = new JsonObject();
            if (expiredCard) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Credit card is expired.");
            }
            else if (creditCardIsValid) {
                // Credit card is valid from given information:

                LocalDate saleDate = LocalDate.now();
                // iterate through previousItems and insert sales into sales table
                for (String movieId : previousItems.keySet()) {
                    JsonObject jsonObj = previousItems.get(movieId);
                    // quantity of cart item
                    int quantity = Integer.parseInt(String.valueOf(jsonObj.get("quantity")).replaceAll("\"",""));
                    // add that item x amount of times based on quantity
                    for (int i = 0; i < quantity; i++) {
                        String insertSaleQuery =
                                "INSERT INTO sales (customerId, movieId, saleDate) \n" +
                                        "VALUES ( ? , ? , ? );";


                        // Declare our statement
                        PreparedStatement insertStatement = dbcon.prepareStatement(insertSaleQuery);
                        insertStatement.setString(1, customerId);
                        insertStatement.setString(2, movieId);
                        insertStatement.setString(3, String.valueOf(saleDate));


                        // Perform the insertion
                        insertStatement.executeUpdate();
                    }
                }

                // clear the shopping cart. Copied from SessionServlet.java
                previousItems = new HashMap<String, JsonObject>();
                session.setAttribute("previousItems", previousItems);

                // success
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Credit card is not valid from given information:

                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid credit card information.");
            }
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
