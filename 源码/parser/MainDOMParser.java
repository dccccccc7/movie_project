import com.mysql.cj.protocol.Resultset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainDOMParser {
    private static final HashMap<String, String> genreAbbreviations = new HashMap<>();

    static {
        genreAbbreviations.put("dram","Drama");
        genreAbbreviations.put("susp","Suspense");
        genreAbbreviations.put("romt","Romance");
        genreAbbreviations.put("musc","Musical");
        genreAbbreviations.put("muscl","Musical");
        genreAbbreviations.put("myst","Mystery");
        genreAbbreviations.put("comd","Comedy");
        genreAbbreviations.put("docu","Documentary");
        genreAbbreviations.put("advt","Adventure");
        genreAbbreviations.put("actn","Action");
        genreAbbreviations.put("act","Action");
        genreAbbreviations.put("west","Western");
        genreAbbreviations.put("fant","Fantasy");
        genreAbbreviations.put("scfi","Sci-Fi");
        genreAbbreviations.put("faml","Family");
        genreAbbreviations.put("cart","Cartoon");
        genreAbbreviations.put("horr","Horror");
        genreAbbreviations.put("biop","Biography");
        genreAbbreviations.put("hist","History");
        genreAbbreviations.put("epic","Epic");
        genreAbbreviations.put("crim","Crime");
        genreAbbreviations.put("noir","Noir");
        genreAbbreviations.put("cnrb","CnRb");
        genreAbbreviations.put("ctxx","Ctxx");
        genreAbbreviations.put("surr","Surreal");
        genreAbbreviations.put("fun","Comedy"); // merge fun and comedy genres
        genreAbbreviations.put("porn","Adult"); // merge porn and adult genres
        genreAbbreviations.put("kinky","Adult"); // merge kinky and adult genres
        genreAbbreviations.put("scat","Adult"); // merge scat and adult genres
    }

    List<String> inconsistencies = new ArrayList<>();
    Document dom;
    private DataSource dataSource;
    private PreparedStatement insertMovieStatement;
    private PreparedStatement insertGenreStatement;
    private PreparedStatement selectGenreMaxStatement;
    private PreparedStatement selectGenreExistsStatement;
    private PreparedStatement insertGenresInMoviesStatement;

    public void run() {
        // parse the xml file and get the dom object
        parseXmlFile();

        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            String insertMovieQuery =
                    "INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE id=id";

            String selectGenreMaxId = "SELECT MAX(id) AS max_id FROM genres";

            String selectGenreExists = "SELECT id FROM genres WHERE name = ? LIMIT 1";

            String insertGenreQuery = "INSERT IGNORE INTO genres (id, name) VALUES (?, ?)" +
                                        "ON DUPLICATE KEY UPDATE id=id";

            String insertGenresInMovies = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)" +
                                            "ON DUPLICATE KEY UPDATE movieId=movieId";

            insertMovieStatement = dbcon.prepareStatement(insertMovieQuery);
            insertGenreStatement = dbcon.prepareStatement(insertGenreQuery);
            selectGenreMaxStatement = dbcon.prepareStatement(selectGenreMaxId);
            selectGenreExistsStatement = dbcon.prepareStatement(selectGenreExists);
            insertGenresInMoviesStatement = dbcon.prepareStatement(insertGenresInMovies);

            // initialize db connection to begin insertion
            parseDocument();

            dbcon.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // iterate through the list and print the data
        // printData();
        if (inconsistencies.size() > 0) {
            System.out.println("[" + inconsistencies.size() + "] Inconsistent Genres:");
            System.out.println(inconsistencies);
        }
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("public/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the DirectorFilms element
                Element element = (Element) nodeList.item(i);

                // parse the DirectorFilms object
                parseDirectorFilms(element);
            }
        }
    }

    /**
     * It takes an employee Film, reads the values in, creates
     * an Film object for return
     */
    private void parseDirectorFilms(Element element) {
        // for each <film> element get text or int values of
        NodeList nodeList = element.getElementsByTagName("film");
        // fid, title, year, director, genres
        String director = getTextValue(element, "dirname");

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element2 = (Element) nodeList.item(i);

                String fid = getTextValue(element2, "fid");
                String title = getTextValue(element2, "t");
                String year = getTextValue(element2, "year");
                if (!year.matches("[0-9]+")) { continue; }
                if (director == null) { director = getTextValue(element2, "dirn"); }
                ArrayList<String> genres = getArrayTextValue(element2, "cat");

                if (fid == null || title == null || director == null) { continue; }
                else if (fid.trim().isEmpty() || title.trim().isEmpty() || director.trim().isEmpty()) { continue; }

                // create a new Film with the value read from the xml nodes
                try {
                    insertMovieStatement.setString(1, fid);
                    insertMovieStatement.setString(2, title);
                    insertMovieStatement.setString(3, year);
                    insertMovieStatement.setString(4, director);

                    insertMovieStatement.executeUpdate();
                    // Genre Queries
                    for (int e = 0; e < genres.size(); e++) {
                        selectGenreExistsStatement.setString(1, genres.get(e));
                        ResultSet rs = selectGenreExistsStatement.executeQuery();
                        boolean genreExistsInDB = false;
                        String idToInsert = null;
                        if (rs.next()) {
                            genreExistsInDB = true;
                            idToInsert = rs.getString("id");
                        }
                        if (!genreExistsInDB) {
                            ResultSet rs2 = selectGenreMaxStatement.executeQuery();
                            if (rs.next()) {
                                idToInsert = rs2.getString("max_id") + 1;
                            }
                            insertGenreStatement.setString(1, idToInsert);
                            insertGenreStatement.setString(2, genres.get(e));

                            insertGenreStatement.executeUpdate();
                        }
                        // insert to genre_in_movies
                        insertGenresInMoviesStatement.setString(1, idToInsert);
                        insertGenresInMoviesStatement.setString(2, fid);

                        insertGenresInMoviesStatement.executeUpdate();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    private ArrayList<String> getArrayTextValue(Element element, String tagName) {
        ArrayList<String> textVal = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);

        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getFirstChild() != null) {
                    String genre = nodeList.item(i).getFirstChild().getNodeValue();
                    if (genre != null) {
                        String cleanedGenre = genre.toLowerCase().trim();
                        if (!cleanedGenre.contains(" ") && genreAbbreviations.containsKey(cleanedGenre)) {
                            textVal.add(genreAbbreviations.get(cleanedGenre));
                        } else {
                            // Print weird spellings seemingly unreadable genres/inconsistencies.
                            inconsistencies.add(genre);
                        }
                    }
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
        MainDOMParser mainDOMParser = new MainDOMParser();

        // call run example
        mainDOMParser.run();
    }

}