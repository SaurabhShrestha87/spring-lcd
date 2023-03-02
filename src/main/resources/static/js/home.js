$(document).ready(function() {
    var list = $("<ul>"); // declare the list variable outside of the event listener
    var intervalId = setInterval(fetchData, 3000); // declare the interval variable outside of the event listener
    $("#toggleButton").change(function() {
        sendToggleState(this.checked); // send true to the other endpoint
        clearInterval(intervalId); // clear the previous interval (if any)
        fetchData(); // fetch data once
        intervalId = setInterval(fetchData, 3000); // fetch data every 1 second
    });

    $("#toggleButtonContiguous").change(function() {
        var toggle = this.checked;
        //sendToggleStateContiguous(this.checked); // send true to the other endpoint
        $.ajax({
            type: "POST",
            url: "/home/togglePanelContiguous",
            data: {toggleState: toggle},
            success: function(response) {
              console.log(response);
            },
            error: function() {
              console.log("Error sending toggle state to controller");
            }
          });
    });
    $("#toggleButtonMirror").change(function() {
        var toggle = this.checked;
        //sendToggleStateContiguous(this.checked); // send true to the other endpoint
        $.ajax({
            type: "POST",
            url: "/home/togglePanelMirror",
            data: {toggleState: toggle},
            success: function(response) {
              console.log(response);
            },
            error: function() {
              console.log("Error sending toggle state to controller");
            }
          });
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

    function calculatePercentage(value) {
      return ((parseFloat(value) - 1) / 30) * 100;
    }

    $(".slider").on('input', function() {
      const sliderValue = $(this).val();
      console.log('sliderValue:', sliderValue);
      const percentage = calculatePercentage(sliderValue);
      $(".slider-value").text(`Brightness: ${percentage.toFixed(2)}%`);
      $.ajax({
        type: "POST",
        url: "/home/sliderData",
        data: { value: sliderValue, percentage: percentage },
        success: function(data) {
          console.log("sliderData sent to controller");
        },
        error: function() {
          console.log("Error sending sliderData to controller");
        }
      });
    });

    $(".single-slider").on('input', function() {
          const sliderValue = $(this).val();
          console.log('sliderValue:', sliderValue);
          const percentage = calculatePercentage(sliderValue);
          $(this).next(".single-slider-value").text(`Brightness: ${percentage}%`);
          // Send the data to the server
          const panelId = $(this).data("panel-id");
          $.ajax({
            type: "POST",
            url: "/home/singleSliderData",
            data: {value: sliderValue, percentage: percentage, panelId: panelId},
            success: function(data) {
              console.log("singleSliderData sent to controller");
            },
            error: function() {
              console.log("Error sending singleSliderData to controller");
            }
          });
        });
});

