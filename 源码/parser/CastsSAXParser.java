import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastsSAXParser extends DefaultHandler {

    List<StarsInMovies> myStarsInMovies;

    private String tempVal;
    private PreparedStatement selectStarByName;
    private PreparedStatement selectMovieById;
    private PreparedStatement insertStarsInMovies;

    //to maintain context
    private StarsInMovies tempEmp;

    public CastsSAXParser() {
        myStarsInMovies = new ArrayList<StarsInMovies>();
    }

    public void run() {

        try {
            String loginUser = "mytestuser";
            String loginPasswd = "My6$Password";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            String selectStarByNameQuery = "SELECT id FROM stars WHERE name = ? LIMIT 1";
            String selectMovieByIdQuery = "SELECT 1 FROM movies WHERE id = ? LIMIT 1";
            String insertStarsInMoviesQuery =
                    "INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (?, ?)" +
                    "ON DUPLICATE KEY UPDATE starId=starId";
            selectStarByName = dbcon.prepareStatement(selectStarByNameQuery);
            selectMovieById = dbcon.prepareStatement(selectMovieByIdQuery);
            insertStarsInMovies = dbcon.prepareStatement(insertStarsInMoviesQuery);

            parseDocument();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("public/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

//    /**
//     * Iterate through the list and print
//     * the contents
//     */
//    private void printData() {
//
//        Iterator<StarsInMovies> it = myStarsInMovies.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
//
//        System.out.println("No of Casts '" + myStarsInMovies.size() + "'.");
//    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of employee
            tempEmp = new StarsInMovies();
            tempEmp.setMovieId(attributes.getValue("f"));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            if (tempEmp.getName() != null && tempEmp.getMovieId() != null) {
                try {
                    String movieId = "";
                    String starId = "";

                    selectStarByName.setString(1, tempEmp.getName());
                    selectMovieById.setString(1, tempEmp.getMovieId());
                    ResultSet rs = selectStarByName.executeQuery();
                    ResultSet rs2 = selectMovieById.executeQuery();
                    if (rs.next() && rs2.next()) {
                        starId = rs.getString("id");
                        movieId = tempEmp.getMovieId();

                        insertStarsInMovies.setString(1, starId);
                        insertStarsInMovies.setString(2, movieId);
                        insertStarsInMovies.executeUpdate();
                    }
                    return;

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

        } else if (qName.equalsIgnoreCase("a")) {
            if (tempVal.equalsIgnoreCase("s a")) {
                tempEmp.setName(null);
            } else {
                tempEmp.setName(tempVal);
            }
        } else if (qName.equalsIgnoreCase("f")) {
            tempEmp.setMovieId(tempVal);
        }

    }

    public static void main(String[] args) {
        CastsSAXParser spe = new CastsSAXParser();
        spe.run();
    }

}