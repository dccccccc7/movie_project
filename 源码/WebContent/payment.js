let payment_form = $("#payment_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("payment-confirmation.html");
    } else {
        // If payment fails, the web page will display
        // error messages on <div> with id "place_order_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#place_order_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the place order button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

function handleTotalCost(resultJSON) {
    let total = 0;
    for (let movieId in resultJSON) {
        let x = parseInt(resultJSON[movieId]["quantity"]) * parseFloat(resultJSON[movieId]["price"]);
        total += x;
    }
    $("#total_cost").text("$" + total.toFixed(2));
}


function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    // show cart information
    handleTotalCost(resultDataJson["previousItems"]);
}

$.ajax(
    "api/session", {
    method: "GET",
        success: handleSessionData
    }
);

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);

