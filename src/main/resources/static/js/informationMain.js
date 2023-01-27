    $(document).ready(function () {
          $(".btnDelete").on("click", function (e) {
                e.preventDefault();
                link = $(this);
                delTitle = link.attr("delTitle");
                $("#yesBtn").attr("href", link.attr("href"));
                $("#confirmText").html("Do you want to delete \<strong\>" + delTitle + "\<\/strong\>?");
                $("#deleteConfirmModal").modal();
          });

          $("#btnAdd, .btnEdit").on("click", function(e) {
            e.preventDefault();
            var href = $(this).attr("href");
            var isEditOrDel = $(this).attr("isEditOrDel");
            var type = $(this).attr("type");
            //for update user
            if (isEditOrDel == "Edit") {
                $.get(href, function (profileCreationRequest, status) {
                    $(".myFormUpdate #id").val(profileCreationRequest.id);
                    $(".myFormUpdate #name").val(profileCreationRequest.username);
                    $(".myFormUpdate #date").val(profileCreationRequest.password);
                });
                $("#updateModalLabel").html("\<strong\>Update " + type + "\<\/strong\>?");
                $(".myFormUpdate #updateModal").modal();
            } else {
                //for creating user
                $(".myFormCreate #name").val("");
                $(".myFormCreate #date").val("");
                $(".myFormCreate #myModalCreate").modal();
            }
        });
    });

        function changePageSize() {
          $("#searchForm").submit();
        }
