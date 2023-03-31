$(document).ready(function() {
    var list = $("<ul>"); // declare the list variable outside of the event listener
    var intervalId = setInterval(fetchData, 3000); // declare the interval variable outside of the event listener

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

    $("#identify-button").click(function() {
        $.get("/user/panel/identify", function(data) {
            $.each(data, function(index, value) {
                console.log("getLogs successfully." + value);
            });
        });
    });

    /* Sending panel connection/ on/off status */
    $('.cb').change(function() {
        // Get the states of all checkboxes
        var states = [];
        $('.cb').each(function() {
            states.push($(this).prop('checked'));
        });
        // Make an AJAX call to the Spring Boot controller
        $.ajax({
            type: 'POST',
            url: '/user/panel/update-panel-connection',
            contentType: 'application/json',
            data: JSON.stringify(states),
            success: function(response) {
                console.log('Checkbox states updated.');
                document.querySelector('#selected-option-save p').textContent = "SELECT";
                document.querySelector('#selected-option-load p').textContent = "CUSTOM";
            },
            error: function(xhr, status, error) {
                console.error('Error updating checkbox states:', error);
            }
        });
    });

    const labels = document.querySelectorAll('.b2');
    labels.forEach(label => {
        const dropdown = label.querySelector('.dropdown');
        dropdown.addEventListener('change', () => {
            const panelId = dropdown.id;
            const dropdownValue = dropdown.value;
            if (panelId === dropdownValue-1) {
                console.log('Panel ID and dropdown value are the same. No action taken.');
                return;
            }
            // Make an AJAX call to the Spring Boot controller
            $.ajax({
                type: 'POST',
                url: '/user/panel/change-sn',
                data: {
                    panelId: panelId,
                    value: dropdownValue
                },
                success: function(response) {
                    console.log('Dropdown selection updated.');
                    document.querySelector('#selected-option-save p').textContent = "SELECT";
                    document.querySelector('#selected-option-load p').textContent = "CUSTOM";
                    window.location.reload();
                },
                error: function(xhr, status, error) {
                    console.error('Error updating dropdown selection:', error);
                }
            });
        });
    });

});

