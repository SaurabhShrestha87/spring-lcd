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
                    $.get(href, function (profile, status) {
                        $(".myFormUpdate #id").val(profile.id);
                        $(".myFormUpdate #name").val(profile.name);
                        $(".myFormUpdate #date").val(profile.date);
                    });
                }
              case "Information": {
                    $.get(href, function (information, status) {
                        $(".myFormUpdate #id").val(information.id);
                        $(".myFormUpdate #name").val(information.name);
//                        $(".myFormUpdate #type").val(information.type);
//                        $(".myFormUpdate #multipartFile").val(information.password);
                        $(".myFormUpdate #fileURL").val(information.url);
//                        $(".myFormUpdate #profileID").val(information.profile.id);
                    });
                    break;
                }
              case "Lend": {
                //TODO
                break;
              }
              case "Panel": {
                //TODO
                break;
              }
            }
            $("#updateModalLabel").html("\<strong\>Update " + type + "\<\/strong\>?");
            $(".myFormUpdate #updateModal").modal();
        });

    });

        function changePageSize() {
          $("#searchForm").submit();
        }
