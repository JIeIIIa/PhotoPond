function parseUrl(url) {
    var index = url.lastIndexOf('/');
    var parts = {
        path: url.substring(0, index),
        name: url.substring(index + 1)
    };

    return parts;
}

function clearSubdirectoriesForm() {
    $(".subDir").remove();
}

/**
 * @param {Object} data
 * @param {String} data.currentDirectoryName
 * @param {String} data.parentURI
 * @param {String[]} data.subDirectoryNames
 * */
function fillSubdirectoriesForm(data, url) {
    console.log("currentDir = " + data.currentDirectoryName);
    console.log("parentUri = " + data.parentURI);
    console.log("subDirs = " + data.subDirectoryNames);
    //$("#subDirName").val(data.currentDirectoryName);
    var el = $("#moveToLabel");
    el.attr('data-url', url);
    el.text('/' + data.currentDirectoryName);
    var subDirNames = $("#subDirNames");
    var parentDir = $(
        '<div/>', {
            text: '..',
            class: 'col-12 subDir',
            'data-url': data.parentURI,
            dblclick: function () {
                var url = $(this).attr('data-url');
                getSubdirectories(url);
            }
        }
    );
    subDirNames.append(parentDir);

    for (i = 0; i <= data.subDirectoryNames.length; i++) {
        var subDir = $(
            '<div/>', {
                text: data.subDirectoryNames[i],
                class: 'col-12 subDir',
                'data-url': data.parentURI + data.currentDirectoryName + '/' + data.subDirectoryNames[i],
                dblclick: function () {
                    var url = $(this).attr('data-url');
                    getSubdirectories(url);
                }
            }
        );
        subDirNames.append(subDir);
    }
}

function getSubdirectories(url) {
    console.log("getSubdirectories(url = " + url + ")");
    $.ajax({
        url: location.protocol + "//" + location.host + url,
        type: 'GET',

        success: function (data, textStatus, xhr) {
            console.log(xhr.status);
            if (xhr.status === 200) {
                console.log(data);
                clearSubdirectoriesForm();
                fillSubdirectoriesForm(data, url);
            }
        },
        error: function (data) {
            // alert("");
            console.log(data);
        }
    });
}

$(document).ready(function () {
    console.log('userDirectory was loaded');

    /*$('.card.userElement').click(function () {
        $(this).toggleClass("selected");
        console.log("clicked");
    });
*/
    $('#openUploadFilesForm_').click(function () {
        console.log('Upload label clicked');
        var inputDlg = $('#files');
        inputDlg.val('');
        inputDlg.trigger('click');
    });

    //Trigger now when you have selected any file
    $("#files_").change(function (e) {
        console.log('Try to upload');
        $('#uploadFilesButton').click();
    });


    $("#deleteBtn_").click(function () {
        console.log('start deleting...');
        var elements = $(".userElement.selected");
        if (jQuery.isEmptyObject(elements)) {
            return;
        }

        var host = location.protocol + "//" + location.host;

        $(elements).each(function () {
            var el = $(this).find(".contentSrc");
            var dataUrl = el.attr("data-url");

            var url = host + dataUrl;
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
        $("#deleteBtn").blur();
    });


    $("#openRenameForm_").click(function () {
        var elements = $(".userElement.selected");
        if (jQuery.isEmptyObject(elements)) {
            return;
        }
        if (elements.length !== 1) {
            alert('Not choose or choose more than one element');
            return;
        }

        var el = elements.find(".contentSrc");
        var dataUrl;
        dataUrl = el.attr("data-url");

        var parts = parseUrl(dataUrl);
        var elName = parts.name;
        var nameAfterRename = $("#nameAfterRename");
        nameAfterRename.val(elName);
        nameAfterRename.attr("data-url", dataUrl);
        nameAfterRename.attr("data-parent-url", parts.path);
        $('#renameElementForm').modal('show');
    });

    $("#renameButton").click(function () {
        var input = $("#nameAfterRename");
        if (jQuery.isEmptyObject(input.val().trim())) {
            alert('Error! Empty name!');
            return;
        }


        var obj = {
            parentURI: input.attr("data-parent-url"),
            elementName: input.val()
        };
        console.log(obj);
        console.log("url:   " + input.attr("data-url"));
        var baseElement = $(".userElement.selected");

        baseElement.removeClass("selected");
        $('#renameElementForm').modal('hide');
        $.ajax({
            url: input.attr("data-url"),
            type: 'PUT',
            data: JSON.stringify(obj),
            contentType: 'application/json',

            success: function (data, textStatus, xhr) {
                console.log(xhr.status);
                if (xhr.status === 200) {
                    var el = baseElement.find(".contentSrc");
                    if (el.is('img')) {
                        el.attr("src", obj.parentURI + "/" + obj.elementName);
                        el.attr("alt", el.attr("src"));
                        alert("success: " + data);
                    } else if (el.is('a')) {
                        el.attr("href", obj.parentURI + "/" + obj.elementName);
                        el.attr("data-name", obj.elementName);
                        el.text(obj.elementName);
                    }
                    el.attr("data-url", obj.parentURI + "/" + obj.elementName);
                }

            },
            error: function (data) {
                alert("Error while renaming.");
                console.log(data);
            }
        });

    });

    $('#openAddDirectoryForm').click(function () {
/*
        $('#addDirectoryName').val("");
        $('#addDirectoryForm').modal('show');
*/
    });

    $('#openMoveForm_').click(function () {
        console.log("Starting removing form...");
        var elements = $(".userElement.selected");
        if (jQuery.isEmptyObject(elements)) {
            alert("No item selected!");
            return;
        }
        var dataUrl = $("#moveToLabel").attr("data-url");
        clearSubdirectoriesForm();
        getSubdirectories(dataUrl);
        $('#moveToForm').modal('show');
    });

    $('#moveToButton').click(function () {
        console.log('try removing...');
        var elements = $(".userElement.selected");
        if (jQuery.isEmptyObject(elements)) {
            alert("No item selected!");
            return;
        }

        var host = location.protocol + "//" + location.host;
        var parentUrl = $('#moveToLabel').attr('data-url');
        $("#moveToForm").modal('hide');
        $(elements).each(function () {
            console.log('in each...');
            var el = $(this).find(".contentSrc");
            var dataUrl = el.attr("data-url");
            var url = host + dataUrl;
            var obj = {
                parentURI: parentUrl.replace('/api/', '/user/').replace('/directories/', '/drive/'),
                elementName: parseUrl(dataUrl).name
            };
            console.log(obj);
            console.log("url:   " + url);

            $.ajax({
                url: url,
                type: 'PUT',
                data: JSON.stringify(obj),
                contentType: 'application/json',

                success: function (data, textStatus, xhr) {
                    console.log(xhr.status);
                    if (xhr.status === 200) {
                        var deletingElement = el.parents(".userElementCard");
                        deletingElement.remove();
                    }

                },
                error: function (data) {
                    alert("Error while moving.");
                    el.removeClass('selected');
                    console.log(data);
                }
            });
        });

        $("#openMoveForm").blur();
    })
});