import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActorsDOMParser {

    Document dom;
    private DataSource dataSource;
    private PreparedStatement selectMaxStarIdStatement;
    private PreparedStatement insertStarStatement;
    private PreparedStatement selectStarByNameStatement;
    private int maxStarId = 0;

    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            String selectMaxStarId = "SELECT MAX(id) AS max_id FROM stars WHERE id LIKE 'na%'";
            String selectStarByName = "SELECT 1 FROM stars WHERE name = ? LIMIT 1";

            String insertToStarsQuery =
                    "INSERT IGNORE INTO stars (id, name, birthYear) VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE id=id";

            insertStarStatement = dbcon.prepareStatement(insertToStarsQuery);
            selectMaxStarIdStatement = dbcon.prepareStatement(selectMaxStarId);
            ResultSet rs = selectMaxStarIdStatement.executeQuery();
            if (rs.next()) {
                if (rs.getString("max_id") != null) {
                    maxStarId = Integer.parseInt(rs.getString("max_id").substring(2));
                } else {
                    maxStarId = 0;
                }
            }
            selectStarByNameStatement = dbcon.prepareStatement(selectStarByName);

            // initialize db connection to begin insertion
            parseDocument();

            dbcon.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // iterate through the list and print the data
//        printData();
        System.out.println("Finished inserting new Stars.");

    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("public/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the DirectorFilms element
                Element element = (Element) nodeList.item(i);

                // parse the DirectorFilms object
                parseActors(element);
            }
        }
    }

    /**
     * It takes an employee Film, reads the values in, creates
     * an Film object for return
     */
    private void parseActors(Element element) {
        // for each <star> element get text or int values of

        // id, name, birthYear
        String id = "na" + (maxStarId + 1);
        String name = getTextValue(element, "stagename");
        String birthYear = getTextValue(element, "dob");

        try {
            selectStarByNameStatement.setString(1, name);
            ResultSet starExists = selectStarByNameStatement.executeQuery();

            if (!starExists.next()) {
                insertStarStatement.setString(1, id);
                insertStarStatement.setString(2, name);
                insertStarStatement.setString(3, birthYear);

                insertStarStatement.executeUpdate();
                maxStarId += 1;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private ArrayList<String> getArrayTextValue(Element element, String tagName) {
        ArrayList<String> textVal = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getFirstChild() != null) {
                    textVal.add(nodeList.item(i).getFirstChild().getNodeValue());
                }
            }
        }
        return textVal;
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            if (nodeList.item(0).getFirstChild() != null) {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            }
        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    public static void main(String[] args) {
        // create an instance
        ActorsDOMParser actorsDOMParser = new ActorsDOMParser();

        // call run example
        actorsDOMParser.run();
    }

}