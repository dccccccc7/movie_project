/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let genreTableBody = jQuery("#genre_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += '<th><a href="movies.html?genreId=' + resultData[i]["genre_id"] + '">' + resultData[i]["genre_name"] + "</a></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        genreTableBody.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});