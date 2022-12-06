let search_form = $("#search_form");

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

    let url = "movies.html?type=quickSearch&";

    let title = formSubmitEvent.target[0].value;

    if ((title) && url.indexOf("movies.html?") === -1) {
        url += "?title=" + title + "&"
    } else if (url.indexOf("title=") !== -1) {
        url = url.replace(/title=.*?(&)/,"title=" + title + "&")
    } else {
        url += "title=" + title
    }

    window.location.replace(url);
}

// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);