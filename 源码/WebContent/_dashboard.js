let star_form = $("#add-star-form");
let movie_form = $("#add-movie-form");


function handleInsertStarResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle star insertion result response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);
    console.log(resultDataJson["message"]);

    //$("#add-star-success").modal('show');
    $("#add-star-success").modal();
    $("#star-id-added").text("Added new star. Star ID: " + resultDataJson["message"]);
}

function submitStarForm(formSubmitEvent) {
    console.log("submit insert star form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/insert-star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: star_form.serialize(),
            success: handleInsertStarResult
        }
    );
}
star_form.submit(submitStarForm);

function handleInsertMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle movie insertion result response");

    $("#add-movie-modal").modal();

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        console.log("Show success message");
        console.log(resultDataJson["message"]);
        $("#add-movie-result").text(resultDataJson["message"]);
        $("#add-movie-star").text(resultDataJson["star"]);
        $("#add-movie-genre").text(resultDataJson["genre"]);
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add-movie-result").text(resultDataJson["message"]);
        $("#add-movie-star").text(resultDataJson["star"]);
        $("#add-movie-genre").text(resultDataJson["genre"]);
    }
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit insert movie form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/insert-movie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: movie_form.serialize(),
            success: handleInsertMovieResult
        }
    );
}
movie_form.submit(submitMovieForm);


function handleEmployeeVerificationData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle employee verification response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If user is an employee, they will be granted access
    if (resultDataJson["status"] === "success") {
        console.log("User is an employee, access to dashboard allowed");
        console.log(resultDataJson["message"]);
    } else {
        // If user attempts to access dashboard but is not an employee
        // they will be forced back to the home page
        console.log("User is not an employee");
        console.log(resultDataJson["message"]);
        window.location.replace("index.html");
    }
}

$.ajax(
    "api/verify-employee-status", {
        method: "GET",
        success: handleEmployeeVerificationData
    }
);