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

    let url = "movies.html?type=search&";

    let title = formSubmitEvent.target[0].value;
    if (title) url += "title=" + title + "&";
    let year = formSubmitEvent.target[1].value;
    if (year) url += "year=" + year + "&";
    let director = formSubmitEvent.target[2].value;
    if (director) url += "director=" + director + "&";
    let star = formSubmitEvent.target[3].value;
    if (star) url += "star=" + star + "&";
    let genreId = formSubmitEvent.target[4].value;
    if (genreId) url += "genreId=" + genreId + "&";
    let numResults = formSubmitEvent.target[5].value;
    if (numResults) url += "numResults=" + numResults + "&";

    window.location.replace(url + "page=1");
}

// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);