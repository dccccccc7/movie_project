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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "SessionServlet", urlPatterns = "/api/session")
public class SessionServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        JsonObject responseJsonObject = new JsonObject();

        HashMap<String, JsonObject> previousItems = (HashMap<String, JsonObject>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, JsonObject>();
        }
        JsonArray previousItemsJsonArray = new JsonArray();
        Gson gson = new Gson();
        responseJsonObject.add("previousItems", gson.toJsonTree(previousItems));

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        String quantity = request.getParameter("quantity");
        String price = request.getParameter("price");
        String fromCart = request.getParameter("fromCart");

        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        HashMap<String, JsonObject> previousItems = (HashMap<String, JsonObject>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, JsonObject>();

            JsonObject obj = new JsonObject();
            obj.addProperty("title", title);
            obj.addProperty("quantity", quantity);
            obj.addProperty("price", price);

            previousItems.put(movieId, obj);

            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (quantity.equalsIgnoreCase("0")) {
                    previousItems.remove(movieId);
                } else {
                    JsonObject obj = new JsonObject();
                    if (previousItems.containsKey(movieId)) {
                        JsonObject oldObj = previousItems.get(movieId);
                        obj.addProperty("title", String.valueOf(oldObj.get("title")).replaceAll("\"",""));
                        if (fromCart.equalsIgnoreCase("false")) {
                            obj.addProperty("quantity", String.valueOf(Integer.parseInt(quantity) + 1));
                        } else {
                            obj.addProperty("quantity", quantity);
                        }
                        obj.addProperty("price", String.valueOf(oldObj.get("price")).replaceAll("\"",""));
                    }
                    else {
                        obj.addProperty("title", title);
                        obj.addProperty("quantity", quantity);
                        obj.addProperty("price", price);
                    }

                    previousItems.put(movieId, obj);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();
        Gson gson = new Gson();
        responseJsonObject.add("previousItems", gson.toJsonTree(previousItems));

        response.getWriter().write(responseJsonObject.toString());
    }
}
