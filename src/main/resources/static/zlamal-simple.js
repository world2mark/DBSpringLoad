'use strict';


document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM Loaded, JS running");

    // document.getElementById('myLink').addEventListener('click', function (event) {
    //     event.preventDefault();
    //     console.log("Anchor clicked but not redirecting");
    // });

    function createTextResponse(myResponse, isError) {
        const theTag = [];
        theTag.push(`<div class="MessageDivElem"><span class="TimeStampElem">${new Date().toISOString()}</span><br>`);
        if (isError) {
            theTag.push(`<span class="MessageErrElem">${myResponse}</span>`);
        } else {
            theTag.push(`<span class="MessageRespElem">${myResponse}</span>`);
        };
        theTag.push('</div>');

        const newDiv = document.createElement('div');

        const parser = new DOMParser();
        const theDoc = parser.parseFromString(theTag.join(''), 'text/html');
        newDiv.prepend(theDoc.body.firstChild);

        const outputContainer = document.getElementById('outputContainer');
        outputContainer.prepend(newDiv);
    };

    function createJSONResponse(myResponseArr) {
        for (const respObj of myResponseArr) {
            const theTag = [];

            const msgText = atob(respObj.message);

            theTag.push(`<div class="MessageDivElem"><span class="TimeStampElem">${new Date().toISOString()}</span><br>`);
            if (respObj.success) {
                theTag.push(`<span class="MessageRespElem">${msgText}</span>`);
            } else {
                theTag.push(`<span class="MessageErrorElem">${msgText}</span>`);
            };
            theTag.push('</div>');

            const newDiv = document.createElement('div');

            const parser = new DOMParser();
            const theDoc = parser.parseFromString(theTag.join(''), 'text/html');
            newDiv.prepend(theDoc.body.firstChild);

            const outputContainer = document.getElementById('outputContainer');
            outputContainer.prepend(newDiv);
        };
    };


    let allAnchors = document.querySelectorAll('a');

    for (const myAnchor of allAnchors) {
        if (myAnchor.target !== '#') {
            myAnchor.addEventListener('click', function (event) {
                event.preventDefault();
                console.log(event.currentTarget.href);

                const xhr = new XMLHttpRequest();

                xhr.open('GET', event.currentTarget.href, true);

                xhr.onload = function () {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            const myJSONArr = JSON.parse(xhr.responseText);
                            createJSONResponse(myJSONArr);
                        } catch (err) {
                            createTextResponse(xhr.responseText);
                        }
                    } else {
                        createTextResponse(xhr.status, true);
                    }
                };

                // Function to be called in case of network errors
                xhr.onerror = function () {
                    createTextResponse('Request failed due to network error.', true);
                };

                // Send the request
                xhr.send();
            });
        };
    };
});
