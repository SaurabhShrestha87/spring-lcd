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
                    $(".myFormCreate #name").val("");
                    $(".myFormCreate #date").val("");
                    $(".myFormCreate #myModalCreate").modal();
              }
              break;
              case "Information": {
                    $(".myFormUpdate #name").val("");
                    $(".myFormUpdate #type").val("");
                    $(".myFormUpdate #multipartFile").val("");
                    $(".myFormUpdate #fileURL").val("");
                    $(".myFormUpdate #profileID").val("");
                    $(".myFormCreate #myModalCreate").modal();
                }
                break;
              case "Lend": {
              //TODO
                }
                break;
              case "Panel": {
              //TODO
                }
            }
        });

          $(".btnEdit").on("click", function(e) {
            e.preventDefault();
            var href = $(this).attr("href");
            var type = $(this).attr("type");
            //for update
            switch (type) {
              case "Profile": {
                    $.get(href, function (profileCreationRequest, status) {
                        $(".myFormUpdate #id").val(profileCreationRequest.id);
                        $(".myFormUpdate #name").val(profileCreationRequest.username);
                        $(".myFormUpdate #date").val(profileCreationRequest.password);
                    });
                }
              case "Information": {
                    $.get(href, function (informationCreationRequest, status) {
                        $(".myFormUpdate #id").val(informationCreationRequest.id);
                        $(".myFormUpdate #name").val(informationCreationRequest.username);
                        $(".myFormUpdate #type").val(informationCreationRequest.password);
                        $(".myFormUpdate #multipartFile").val(informationCreationRequest.password);
                        $(".myFormUpdate #fileURL").val(informationCreationRequest.password);
                        $(".myFormUpdate #profileID").val(informationCreationRequest.password);
                    });
                    break;
                }
              case "Lend": {
                //TODO
                break;
              }
              case "Panel": {
                //TODO
              }
            }
            $("#updateModalLabel").html("\<strong\>Update " + type + "\<\/strong\>?");
            $(".myFormUpdate #updateModal").modal();
        });

    });

        function changePageSize() {
          $("#searchForm").submit();
        }
