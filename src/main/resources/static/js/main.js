$(document).ready(function() {
    $(".btnDelete").on("click", function(e) {
        console.log("btnDelete");
        e.preventDefault();
        link = $(this);
        delTitle = link.attr("delTitle");
        $("#yesBtn").attr("href", link.attr("href"));
        $("#confirmText").html("Do you want to delete \<strong\>" + delTitle + "\<\/strong\>?");
        $("#deleteConfirmModal").modal();
    });

    $(".btnAddInformation").on("click", function(e) {
        console.log("btnAddInformation");
        e.preventDefault();
        var href = $(this).attr("href");
        var profileId = $(this).attr("profileId");
        $(".myFormProfileInformationAdd #profileId").val(profileId);
        $(".myFormProfileInformationAdd #addModal").modal();
    });

    $("#btnAdd").on("click", function(e) {
        console.log("btnAdd");
        e.preventDefault();
        var type = $(this).attr("type");
        //for creating user
        switch (type) {
            case "Profile": {
                $(".myFormProfileCreate #name").val("");
                $(".myFormProfileCreate #date").val("");
                $(".myFormProfileCreate #myModalCreate").modal();
            }
            break;
            case "Information": {
                $(".myFormInformationCreate #name").val("");
                $(".myFormInformationCreate #type").val("");
                $(".myFormInformationCreate #multipartFile").val("");
                $(".myFormInformationCreate #fileURL").val("");
                $(".myFormInformationCreate #profileID").val("");
                $(".myFormInformationCreate #myModalCreate").modal();
            }
            break;
            case "Lend": {
                //TODO
            }
            break;
            case "Panel": {
                $(".myFormPanelRefresh #myModalCreate").modal();
            }
        }
    });

    $(".btnEdit").on("click", function(e) {
        console.log("btnEdit");
        e.preventDefault();
        var href = $(this).attr("href");
        var type = $(this).attr("type");
        $("#updateModalLabel").html("\<strong\>Update " + type + "\<\/strong\>?");
        //for update
        switch (type) {
            case "Information": {
                var isProfile = $(this).attr("isProfile");
                console.log(isProfile);
                $.get(href, function(information, status) {
                    $(".myFormInformationUpdate #id").val(information.id);
                    $(".myFormInformationUpdate #name").val(information.name);
                    //$(".myFormUpdate #type").val(information.type);
                    //$(".myFormUpdate #multipartFile").val(information.password);
                    $(".myFormInformationUpdate #fileURL").val(information.url);
                    //$(".myFormUpdate #profileID").val(information.profile.id);
                    if (isProfile == "1") {
                        $(".myFormInformationUpdate #duration").val(information.duration);
                        $(".myFormInformationUpdate #count").val(information.count);
                        $(".myFormInformationUpdate #durationDiv").show();
                        $(".myFormInformationUpdate #countDiv").show();
                        console.log("This one ran!");
                    } else {
                        $(".myFormInformationUpdate #durationDiv").hide();
                        $(".myFormInformationUpdate #countDiv").hide();
                        console.log("This one ran! 2 ");
                    }
                });
                $(".myFormInformationUpdate #updateModal").modal();
                break;
            }
            case "Profile": {
                $.get(href, function(profile, status) {
                    $(".myFormProfileUpdate #id").val(profile.id);
                    $(".myFormProfileUpdate #name").val(profile.name);
                    $(".myFormProfileUpdate #date").val(profile.date);
                });
                $(".myFormProfileUpdate #updateModal").modal();
                break;
            }
            case "Lend": {
                //TODO
                break;
            }
            case "Panel": {
                $.get(href, function(panel, status) {
                    $(".myFormPanelUpdate #id").val(panel.id);
                    $(".myFormPanelUpdate #name").val(panel.name);
                    $(".myFormPanelUpdate #resolution").val(panel.resolution);
                    $(".myFormPanelUpdate #status").val(panel.status);
                });
                $(".myFormPanelUpdate #updateModal").modal();
                break;
            }
        }
    });

    $('#toggleLendBtn').on('change', function() {
        var isChecked = $(this).is(':checked');
        var lend_id = $(this).attr("lend_id");
        $.ajax({
            type: "POST",
            url: "/lend/toggleLend/",
            data: {toggleState: isChecked, id : lend_id},
            success: function(response) {
                console.log(lend_id);
                $('#status_' + lend_id).text(response);
            },
            error: function() {
              console.log("Error sending toggle state to controller");
            }
        });
    });
});

function changePageSize() {
    $("#searchForm").submit();
}