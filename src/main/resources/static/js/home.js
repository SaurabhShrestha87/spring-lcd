$(document).ready(function() {
    var list = $("<ul>"); // declare the list variable outside of the event listener
    var intervalId = setInterval(fetchData, 3000); // declare the interval variable outside of the event listener
    $("#toggleButton").change(function() {
        sendToggleState(this.checked); // send true to the other endpoint
        clearInterval(intervalId); // clear the previous interval (if any)
        fetchData(); // fetch data once
        intervalId = setInterval(fetchData, 3000); // fetch data every 1 second
    });

    function fetchData() {
        $.get("/home/getData", function(data) {
            list.empty(); // empty the list before adding new items
            $.each(data, function(index, value) {
                var listItem = $("<li>").text(value);
                list.append(listItem);
            });
            $("#data-container").html(list); // use the html() method to replace the content of the container
                console.log("getData successfully.");
        });
    }
    
    function sendToggleState(state) {
        $.ajax({
            type: "POST",
            url: "/home/togglePanel",
            data: {toggleState: state},
            success: function() {
              console.log("Toggle state sent to controller");
            },
            error: function() {
              console.log("Error sending toggle state to controller");
            }
          });
    }

    $("#fetch-button").click(function() {
        $.get("/home/getLogs", function(data) {
            var logs = $("<ul>"); // declare the list variable outside of the event listener
            $.each(data, function(index, value) {
                var logItem = $("<li>").text(value);
                logs.append(logItem);
            });
            $("#logs-container").html(logs); // use the html() method to replace the content of the container
            console.log("getLogs successfully.");
        });
    });
    $("#reset-button").click(function() {
        $.get("/home/reset", function(data) {
            var logs = $("<ul>"); // declare the list variable outside of the event listener
            $.each(data, function(index, value) {
                var logItem = $("<li>").text(value);
                logs.append(logItem);
            });
            $("#logs-container").html(logs); // use the html() method to replace the content of the container
            console.log("getLogs successfully.");
        });
    });
});

