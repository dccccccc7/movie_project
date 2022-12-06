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
function handleMoviesResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    if(resultData.hasOwnProperty("previousQuery")) {
        window.location.href = window.location.href.split('?')[0] + "?" + resultData["previousQuery"];
    }

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    let paginationElement = jQuery("#pagination_buttons");

    // Iterate through resultData, no more than 10 entries
    let perPageLength = (resultData.length > searchData.numResults) ? resultData.length - 1 : resultData.length;
    for (let i = 0; i < perPageLength; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>"; // movie_title start
        rowHTML += '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]['movie_title'] + "</a>";
        rowHTML += "</th>"; // movie_title end
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" // movie_genres start
        for (let x = 0; x < resultData[i]["movie_genres"].length; x++) {
            if (x > 2) break;
            rowHTML += '<a href="movies.html?genreId=' + resultData[i]["movie_genres"][x]['genre_id'] + '">' + resultData[i]["movie_genres"][x]['genre_name'] + "</a>" + ', '
        }
        rowHTML = rowHTML.slice(0, -2);
        rowHTML += "</th>"; // movie_title end
        rowHTML += "<th>"; // movie_stars start
        for (let y = 0; y < resultData[i]["movie_stars"].length; y++) {
            if (y > 2) break;
            rowHTML += '<a href="single-star.html?id=' + resultData[i]['movie_stars'][y]['star_id'] + '">' + resultData[i]["movie_stars"][y]['star_name'] + "</a>, ";
        }
        rowHTML = rowHTML.slice(0, -2);
        rowHTML += "</th>"; // movie_stars end
        rowHTML += "<th>" + (resultData[i]["movie_rating"] === null ? "N/A" : resultData[i]["movie_rating"]) + "</th>";
        rowHTML += "<th><button id='add-" + resultData[i]["movie_id"] + "' class='btn button-primary button-blue'>Add to Cart</button></th>"
        rowHTML += "</tr>";

        $(document).ready(function () {
            let addButton = $("#add-" + resultData[i]["movie_id"]);

            addButton.click(function() {
                addCartItem(resultData[i]["movie_id"], resultData[i]["movie_title"], 10, 1);
            });
        });

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    if (parseInt(searchData.page) > 1) {
        let currentPage = "page=" + searchData.page;
        let prevLink = "";
        if (window.location.href.indexOf("movies.html?") === -1) {
            prevLink = window.location.href + "?page=" + (parseInt(searchData.page) - 1).toString();
        } else if (window.location.href.indexOf("page=") !== -1) {
            prevLink = window.location.href.replace(currentPage, "page=" + (parseInt(searchData.page) - 1).toString())
        } else {
            prevLink = window.location.href + "&page=" + (parseInt(searchData.page) - 1).toString();
        }
        let paginationHTML = "<a id='prevLink'><button type='button' class='btn button-primary'>Prev</button></a>"
        paginationElement.append(paginationHTML)
        document.getElementById('prevLink').href = prevLink;
    }
    if (resultData.length > searchData.numResults) {
        let currentPage = "page=" + searchData.page;
        let nextLink = "";
        if (window.location.href.indexOf("movies.html?") === -1) {
            nextLink = window.location.href + "?page=" + (parseInt(searchData.page) + 1).toString();
        } else if (window.location.href.indexOf("page=") !== -1) {
            nextLink = window.location.href.replace(currentPage, "page=" + (parseInt(searchData.page) + 1).toString())
        } else {
            nextLink = window.location.href + "&page=" + (parseInt(searchData.page) + 1).toString();
        }
        let paginationHTML = "<a id='nextLink'><button type='button' class='btn button-primary'>Next</button></a>"
        paginationElement.append(paginationHTML)
        document.getElementById('nextLink').href = nextLink;
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let searchData = {}

if (getParameterByName('type') !== null) {searchData.type = getParameterByName('type');}
if (getParameterByName('title') !== null) {searchData.title = getParameterByName('title');}
if (getParameterByName('year') !== null) {searchData.year = getParameterByName('year');}
if (getParameterByName('director') !== null) {searchData.director = getParameterByName('director');}
if (getParameterByName('star') !== null) {searchData.star = getParameterByName('star');}
if (getParameterByName('genreId') !== null) {searchData.genreId = getParameterByName('genreId');}
searchData.numResults = (getParameterByName('numResults') !== null) ? getParameterByName('numResults') : '10';
searchData.page = (getParameterByName('page') !== null) ? getParameterByName('page') : '1';
if (getParameterByName('history') !== null) {searchData.history = getParameterByName('history');}

let sortByElement = jQuery("#sortBy");
// let sortOrderElement = jQuery("#sortOrder");
let numResultsElement = jQuery("#numResults");
if (getParameterByName('sortBy') !== null) {
    searchData.sortBy = getParameterByName('sortBy');
    sortByElement.val(searchData.sortBy);
}
// if (getParameterByName('sortOrder') !== null) {
//     searchData.sortOrder = getParameterByName('sortOrder');
//     sortOrderElement.val(searchData.sortOrder);
// }
numResultsElement.val(searchData.numResults);


// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    data: searchData,
    success: (resultData) => handleMoviesResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});