function updateCartQuantity(movieId, quantity) {
    $.ajax("api/session", {
        method: "POST",
        data: {
            movieId: movieId,
            quantity: quantity,
            fromCart: "true"
        },
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });
}

function deleteCartItem(movieId) {

    $.ajax("api/session", {
        method: "POST",
        data: {
            movieId: movieId,
            quantity: 0,
        },
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultJSON) {
    let cartTableElement = jQuery("#cart_table_body");
    // change it to html list
    let res = "";
    for (let movieId in resultJSON) {
        // each item will be in a bullet point
        if (resultJSON.hasOwnProperty(movieId)) {
            res += "<tr>";
            res += "<th>" + resultJSON[movieId]["title"] + "</th>";
            res += "<th>$" + resultJSON[movieId]["price"] + "</th>";
            res += "<th><input class='quantity-button' min='1' id='quantity-" + movieId + "' type='number' value='" + resultJSON[movieId]["quantity"] + "'/></th>";
            res += "<th><button id='delete-" + movieId + "' type='button' class='btn button-primary button-blue'>Delete</button></th>";
            res += "</tr>";

            $(document).ready(function () {
                let deleteButton = $("#delete-" + movieId);
                let quantityInput = $("#quantity-" + movieId);

                quantityInput.change(function() {
                    if (quantityInput.val() <= 0) {
                        quantityInput.val(1);
                    } else {
                        updateCartQuantity(movieId, quantityInput.val());
                    }
                })

                deleteButton.click(function() {
                    deleteCartItem(movieId);
                });
            });
        }
    }

    // clear the old array and show the new array in the frontend
    cartTableElement.html("");
    cartTableElement.append(res);
}

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);
}

$.ajax("api/session", {
    method: "GET",
    success: handleSessionData
});




let cart = $("#cart");

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/session", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });
}

// Bind the submit action of the form to a event handler function
cart.submit(handleCartInfo);