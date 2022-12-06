/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function addCartItem(movie_id, movie_title, movie_price, movie_quantity) {
    $.ajax("api/session", {
        method: "POST",
        data: {
            movieId: movie_id,
            title: movie_title,
            price: movie_price,
            quantity: movie_quantity,
            fromCart: "false"
        },
        success: () => {
            $("#add-item-success").modal('show');
        }
        // error: TODO: handle error
    });
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#single-movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    let genresHTML = "";
    for (let x = 0; x < resultData[0]["movie_genres"].length; x++) {
        genresHTML += '<a href="movies.html?genreId=' + resultData[0]["movie_genres"][x]['genre_id'] + '">' + resultData[0]["movie_genres"][x]['genre_name'] + "</a>" + ', '
    }
    genresHTML = genresHTML.slice(0,-2);

    movieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        "<p>Genres: " + genresHTML + "</p>" +
        "<p>Rating: " + (resultData[0]["movie_rating"] == null ? "N/A" : resultData[0]["movie_rating"]) + "</p>" +
        "<button id='add-" + resultData[0]["movie_id"] + "' class='btn button-primary button-blue'>Add to Cart</button>");

    $(document).ready(function () {
        let addButton = $("#add-" + resultData[0]["movie_id"]);

        addButton.click(function() {
            addCartItem(resultData[0]["movie_id"], resultData[0]["movie_title"], 10, 1);
        });
    });

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[0]["movie_stars"].length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += '<th><a href="single-star.html?id=' + resultData[0]["movie_stars"][i]['star_id'] + '">' + resultData[0]["movie_stars"][i]['star_name'] + "</a></th>";
        rowHTML += "<th>" + (resultData[0]["movie_stars"][i]["star_dob"] == null ? "N/A" : resultData[0]["movie_stars"][i]["star_dob"]) + "</th>";
        rowHTML += "<th>" + (resultData[0]["movie_stars"][i]["star_total_movies"] == null ? "0" : resultData[0]["movie_stars"][i]["star_total_movies"]) + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});