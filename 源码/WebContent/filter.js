let filter_form = $("#filter_form");

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitSearchForm(formSubmitEvent) {
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    let url = window.location.href;

    let sortBy = formSubmitEvent.target[0].value;
    // let sortOrder = formSubmitEvent.target[1].value;
    let numResults = formSubmitEvent.target[1].value;

    if ((sortBy) && url.indexOf("movies.html?") === -1) {
        url += "?sortBy=" + sortBy + "&"
    } else if (url.indexOf("sortBy=") !== -1) {
        url = url.replace(/sortBy=.*?(&)/,"sortBy=" + sortBy + "&")
    } else {
        url += "sortBy=" + sortBy + "&"
    }

    // if ((sortOrder) && url.indexOf("movies.html?") === -1) {
    //     url += "?sortOrder=" + sortOrder + "&"
    // } else if (url.indexOf("sortOrder=") !== -1) {
    //     url = url.replace(/sortOrder=.*?(&)/,"sortOrder=" + sortOrder + "&")
    // } else {
    //     url += "sortOrder=" + sortOrder + "&"
    // }

    if ((numResults) && url.indexOf("movies.html?") === -1) {
        url += "?numResults=" + numResults + "&"
    } else if (url.indexOf("numResults=") !== -1) {
        url = url.replace(/numResults=.*?(&)/,"numResults=" + numResults + "&")
    } else {
        url += "numResults=" + numResults + "&"
    }

    if (url.indexOf("page=") !== -1) {
        const urlParams = new URLSearchParams(window.location.search);
        const currentPage = urlParams.get("page");
        if (currentPage !== "") {
            url = url.replace("page=" + currentPage, "page=1&");
        }
    } else {
        url += "page=1";
    }

    window.location.replace(url);
}

// Bind the submit action of the form to a handler function
filter_form.submit(submitSearchForm);