function handleEmployeeVerificationData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle employee verification response");

    // If user is an employee, there will be a link to the employee dashboard
    if (resultDataJson["status"] === "success") {
        $("#employee-dashboard-link").text("管理员平台");
    } else {
        // If user is not an employee
        console.log("User is not an employee");
        console.log(resultDataJson["message"]);
    }
}

$.ajax(
    "api/verify-employee-status", {
        method: "GET",
        success: handleEmployeeVerificationData
    }
);

/*
 * CS 122B Project 4. Autocomplete.
 *
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */

let lookupHistory = {};
/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here
    let modifiedQuery = query.toLowerCase().trim()
    if (modifiedQuery in lookupHistory) {
        console.log("autocomplete suggestion list from cached frontend");
        doneCallback( { suggestions: lookupHistory[modifiedQuery] } );
    } else {
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        console.log("sending AJAX request to backend Java Servlet")
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/quick-search?query=" + escape(query),
            "success": function(data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    let jsonData = JSON.parse(data);
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here
    let modifiedQuery = query.toLowerCase().trim()
    lookupHistory[modifiedQuery] = (jsonData);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    let url = "single-movie.html?id=";
    if (suggestion["data"]["movieId"]) {
        window.location.replace(url + suggestion["data"]["movieId"]);
    }
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#search-title').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // handled in another JS File.
}

// bind pressing enter key to a handler function
$('#search-title').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#search-title').val())
    }
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button


