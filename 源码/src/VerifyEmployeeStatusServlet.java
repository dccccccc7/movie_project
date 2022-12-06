import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "VerifyEmployeeStatusServlet", urlPatterns = "/api/verify-employee-status")
public class VerifyEmployeeStatusServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Output stream to STDOUT
        JsonObject responseJsonObject = new JsonObject();

        HttpSession session = request.getSession();

        boolean isEmployee = ((User) session.getAttribute("user") ).isEmployee();

        if (isEmployee) {
            // IS an employee

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");

        } else {
            // is not an employee

            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "You are not an employee");
        }

        response.getWriter().write(responseJsonObject.toString());
        // set response status to 200 (OK)
        response.setStatus(200);
    }

}
