function parseUrl(url) {
    var index = url.lastIndexOf('/');
    var parts = {
        path: url.substring(0, index),
        name: url.substring(index + 1)
    };

    return parts;
}

$(document).ready(function () {
    console.log('userDirectory was loaded');

    $('.card.userElement').click(function () {
        $(this).toggleClass("selected");
        console.log("clicked");
    });

    $('#uploadFiles').click(function () {
        console.log('Upload label clicked');
        var inputDlg = $('#files');
        inputDlg.val('');
        inputDlg.trigger('click');
    });

    //Trigger now when you have selected any file
    $("#files").change(function (e) {
        console.log('Try to upload');
        $('#uploadFilesButton').click();
    });


    $("#deleteElement").click(function () {
        console.log('start deleting...');
        var elements = $(".userElement.selected");
        if(jQuery.isEmptyObject(elements)) {
            return;
        }

        var host = location.protocol + "//" + location.host;

        $(elements).each(function () {
            var el = $(this).find(".contentSrc"), path;
            console.log("el.tagName = " + el.tagName);
            if(el.is('img')) {
                path = el.attr("src");
            } else if(el.is('a')) {
                path = el.attr("href");
            }

            var url = host + path;
            console.log('delete request to ' + url);
            $.ajax({
                url: url,
                type: 'DELETE',

                success: function (data, textStatus, xhr) {
                    console.log(xhr.status);
                    if (xhr.status === 200) {
                        var deletingElement = el.parents(".userElementCard");
                        deletingElement.remove();
                    }

                },
                error: function (data) {
                    alert("Error while deleting.");
                    console.log(data);
                }
            });
        });
        $("#deleteElement").blur();
    });



    $("#renameElement").click(function () {
        var elements = $(".userElement.selected");
        if(jQuery.isEmptyObject(elements)) {
            return;
        }
        if(elements.length !== 1) {
            alert('Not choose or choose more than one element');
            return;
        }

        var el = elements.find(".contentSrc");
        var url;
        if(el.is('img')) {
            url = el.attr("src");
        } else if(el.is('a')) {
            url = el.attr("href");
        }

        var parts = parseUrl(url);
        var elName = parts.name;
        var nameAfterRename = $("#nameAfterRename");
        nameAfterRename.val(elName);
        nameAfterRename.data("url", url);
        nameAfterRename.data("parent-url", parts.path);
        $('#renameElementForm').modal('show');
    });

    $("#renameButton").click(function () {
        var input = $("#nameAfterRename");
        if(jQuery.isEmptyObject(input.val().trim())) {
            alert('Error! Empty name!');
            return;
        }


        var obj = {
            parentURI: input.data("parent-url"),
            elementName: input.val()
        };
        console.log(obj);
        console.log("url:   " + input.data("url"));
        var baseElement = $(".userElement.selected");

        baseElement.removeClass("selected");
        $('#renameElementForm').modal('hide');
        $.ajax({
            url: input.data("url"),
            type: 'PUT',
            data: JSON.stringify(obj),
            contentType: 'application/json',

            success: function (data, textStatus, xhr) {
                console.log(xhr.status);
                if (xhr.status === 200) {
                    var el = baseElement.find(".contentSrc");
                    if(el.is('img')) {
                        el.attr("src", obj.parentURI + "/" + obj.elementName);
                        el.attr("alt", el.attr("src"));
                        alert("success: " + data);
                    } else if(el.is('a')) {
                        el.attr("href", obj.parentURI + "/" + obj.elementName);
                        el.attr("data-name", obj.elementName);
                        el.text(obj.elementName);
                    }
                }

            },
            error: function (data) {
                alert("Error while renaming.");
                console.log(data);
            }
        });

    });

    $('#addDirectory').click(function () {
        $('#addDirectoryName').val("");
        $('#addDirectoryForm').modal('show');
    });

});