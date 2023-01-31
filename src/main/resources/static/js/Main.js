    $(document).ready(function () {
          $(".btnDelete").on("click", function (e) {
                e.preventDefault();
                link = $(this);
                delTitle = link.attr("delTitle");
                $("#yesBtn").attr("href", link.attr("href"));
                $("#confirmText").html("Do you want to delete \<strong\>" + delTitle + "\<\/strong\>?");
                $("#deleteConfirmModal").modal();
          });

          $("#btnAdd").on("click", function(e) {
            e.preventDefault();
            var href = $(this).attr("href");
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
            e.preventDefault();
            var href = $(this).attr("href");
            var type = $(this).attr("type");
            $("#updateModalLabel").html("\<strong\>Update " + type + "\<\/strong\>?");
            //for update
            switch (type) {
                case "Information": {
                    $.get(href, function (information, status) {
                        $(".myFormInformationUpdate #id").val(information.id);
                        $(".myFormInformationUpdate #name").val(information.name);
                        //$(".myFormUpdate #type").val(information.type);
                        //$(".myFormUpdate #multipartFile").val(information.password);
                        $(".myFormInformationUpdate #fileURL").val(information.url);
                        //$(".myFormUpdate #profileID").val(information.profile.id);
                    });
                    $(".myFormInformationUpdate #updateModal").modal();
                    break;
                }
              case "Profile": {
                    $.get(href, function (profile, status) {
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
                    $.get(href, function (panel, status) {
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
    });

        function changePageSize() {
          $("#searchForm").submit();
        }
