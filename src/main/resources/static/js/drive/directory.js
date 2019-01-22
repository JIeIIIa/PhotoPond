var loader = Vue.component('loader-component', {});

var app = new Vue({
    el: '#directoryApp',
    data: function () {
        return {
            url: '',
            elements: [],
            message: '',
            errorCode: '',
            editedValue: '',
            editedItem: '',
            dialogOperationInProgress: false,
            showLoader: false,
            pictureError: {
                status: '',
                count: ''
            },
            size: {
                countPerRow: 2,
                suffix: [12, 6, 4, 3, 2, 1]
            },
            imageIndex: -1,
            sortOptions: new SortOptions("data.name")
        }
    },
    computed: {
        elementsWithUri: function () {
            return this.elements.map(function (item) {
                item.data.uri = item.data.parentUri + '/' + item.data.name;
                return item;
            })
        },
        sortedDirectories: function () {
            return this
                .filter('DIR')
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        sortedFiles: function () {
            return this
                .filter('FILE')
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        parentDirectories: function () {
            var splitUrl = this.url.split('/');
            if (splitUrl.length > 0 && splitUrl[splitUrl.length - 1] === '') {
                splitUrl.splice(splitUrl.length - 1, 1);
            }
            var list = [];
            var currentUrl = '';
            var i;
            for (i = 0; i < 6; i++) {
                currentUrl += splitUrl[i] + '/';
            }
            list.push({
                name: '<i class="fa fa-home"></i>',
                tooltip: '/',
                url: currentUrl
            });
            for (i = 6; i < splitUrl.length; i++) {
                currentUrl += splitUrl[i];
                list.push({
                    name: splitUrl[i],
                    tooltip: splitUrl[i],
                    url: currentUrl
                });
                currentUrl += '/';
            }

            return list;
        },
        selectedItemCount: function () {
            var count = 0;
            this.elements.forEach(function (item) {
                if (item.selected) {
                    count++;
                }
            });
            return count;
        },
        selectedFileCount: function () {
            var count = 0;
            var ref = this;
            this.elements.forEach(function (item) {
                if (item.selected && ref.isFile(item)) {
                    count++;
                }
            });
            return count;
        },
        hasDirectory: function () {
            for (var i = 0; i < this.elements.length; i++) {
                if (this.isDirectory(this.elements[i])) {
                    return true;
                }
            }
            return false;
        },
        hasFile: function () {
            for (var i = 0; i < this.elements.length; i++) {
                if (this.isFile(this.elements[i])) {
                    return true;
                }
            }
            return false;
        },
        colSizeClasses: function () {
            return ['col-' + this.size.suffix[this.size.countPerRow - 1],
                'col-sm-' + this.size.suffix[this.size.countPerRow],
                'col-md-' + this.size.suffix[this.size.countPerRow + 1],
                'col-xl-' + this.size.suffix[this.size.countPerRow + 2]];
        },
        heightSizeClasses: function () {
            return ['height-vw-' + this.size.suffix[this.size.countPerRow - 1],
                'height-vw-sm-' + this.size.suffix[this.size.countPerRow],
                'height-vw-md-' + this.size.suffix[this.size.countPerRow + 1],
                'height-vw-xl-' + this.size.suffix[this.size.countPerRow + 2]];
        },
        images: function () {
            var res = [];
            this.sortedFiles.forEach(function (item) {
                res.push(item.data);
            });

            return res;
        },
        faArrowSortIcon: function () {
            var res = [];
            res.push("fa");
            if (this.sortOptions.isAscend()) {
                res.push("fa-arrow-up");
            } else {
                res.push("fa-arrow-down");
            }
            return res;
        },
        isSelectAll: function () {
            return this.elements.length > 0 && this.selectedItemCount === this.elements.length;
        },
        faSelectedIcon: function () {
            if (this.isSelectAll) {
                return ['far', 'fa-check-circle'];
            } else {
                return ['far', 'fa-circle'];
            }
        }
    },
    methods: {
        dateConvert: function (unixTimeStamp) {
            var dt = eval(unixTimeStamp);
            var myDate = new Date(dt);
            return (myDate.toLocaleString());
        },
        sortBy: function (col) {
            this.sortOptions.changeOrder(col);
        },
        isSortedByField: function (fieldName) {
            return this.sortOptions.fieldName === fieldName;
        },
        filter: function (type) {
            var ref = this;
            var res = [];
            this.elements.forEach(function (item) {
                if ((type === 'DIR' && ref.isDirectory(item)) ||
                    (type === 'FILE' && ref.isFile(item))) {
                    res.push(item);
                }
            });
            return res;
        },
        pageNotFound: function () {
            var redirectedUrl = '/page-not-found';
            var form = $('<form action="' + redirectedUrl + '" method="post" hidden="hidden">' +
                '<input type="text" name="url" value="' + window.location.href + '" />' +
                '</form>');
            $('body').append(form);
            form.submit();
        },
        loadDirectoryData: function () {
            var ref = this;
            ref.showLoader = true;
            axios.get(apiUrl('directory'))
                .then(function (response) {
                    if (response.status === 200) {
                        response.data.forEach(function (item) {
                            ref.addElement(item);
                        });
                        ref.allUsers = response.data;
                    } else {
                        ref.pageNotFound();
                    }
                    ref.showLoader = false;
                })
                .catch(function (error) {
                    ref.showLoader = false;
                    console.log(error);
                    ref.pageNotFound();
                })
        },
        addElement: function (data) {
            data.uri = data.parentUri + '/' + data.name;
            this.elements.push({
                data: data,
                selected: false
            })
        },
        isDirectory: function (item) {
            return item.data.type === 'DIR';
        },
        isFile: function (item) {
            return item.data.type === 'FILE';
        },
        clearErrorCode: function () {
            this.errorCode = '';
        },
        makeError: function (errMsg, status) {
            console.log('code ' + status + ': ' + errMsg);
            this.errorCode = status;
        },
        onFileUpload: function () {
            console.log('start files upload');
            this.$refs.files.click();
        },
        onFileUploadError: function (count, status) {
            console.log('Error in uploading ' + count + ' file(-s) (status = ' + status + ')');
            this.pictureError.count = count;
            this.pictureError.status = status;
        },
        onFileUploadFinish: function () {
            console.log('Files upload finishing\n');
            this.clearPictureError();
            var uploadedFiles = this.$refs.files.files;
            var filesCount = uploadedFiles.length;
            var formData = new FormData();
            for (var i = 0; i < uploadedFiles.length; i++) {
                formData.append('files', uploadedFiles[i]);
            }

            var ref = this;
            ref.showLoader = true;

            axios.post(apiUrl('files'), formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(function (response) {
                if (response.status === 200) {
                    response.data.forEach(function (item) {
                        ref.addElement(item);
                    });
                    var count = filesCount - response.data.length;
                    if (count !== 0) {
                        ref.onFileUploadError(count, response.status);
                    }
                } else {
                    this.onFileUploadError(uploadedFiles.length, response.status);
                }
                ref.showLoader = false;
            }).catch(function (reason) {
                ref.showLoader = false;
                ref.onFileUploadError(uploadedFiles.length, reason.response.status);
            });
            this.$refs.files.value = '';
        },
        clearPictureError: function () {
            this.pictureError.count = '';
            this.pictureError.status = '';
        },
        onCreateDirectory: function () {
            this.editedValue = '';
            this.clearErrorCode();
            this.dialogOperationInProgress = false;
            $('#createDirectory .singleInputModalForm').modal('show');
        },
        onCreateDirectorySuccess: function (directoryName) {
            this.clearErrorCode();
            console.log(directoryName);
            var ref = this;
            var url = apiUrl('directory');
            ref.dialogOperationInProgress = true;

            axios.post(url,
                directoryName,
                {headers: {"Content-Type": "text/plain"}}
            )
                .then(function (response) {
                    if (response.status === 201) {
                        ref.errorCode = "";
                        $("#createDirectory .singleInputModalForm").modal('hide');
                        ref.addElement(response.data);
                    } else {
                        var errMsg = "Response on create directory = " + directoryName;
                        ref.makeError(errMsg, response.status);
                    }
                    ref.dialogOperationInProgress = false;
                })
                .catch(function (reason) {
                    var errMsg = "Response on create directory = " + directoryName;
                    ref.makeError(errMsg, reason.response.status);
                    ref.dialogOperationInProgress = false;
                });
        },
        onImageIndexIncrement: function () {
            if (this.images.length > 1) {
                this.imageIndex = (this.imageIndex + 1) % this.images.length;
            }
        },
        onImageIndexDecrement: function () {
            if (this.images.length > 1) {
                this.imageIndex = (this.imageIndex + this.images.length - 1) % this.images.length;
            }
        },
        onRename: function () {
            if (this.selectedItemCount !== 1) {
                console.log('Renaming failure');
                return;
            }

            for (var i = 0; i < this.elements.length; i++) {
                if (this.elements[i].selected) {
                    this.editedItem = this.elements[i];
                    this.editedValue = this.elements[i].data.name;
                    break;
                }
            }
            console.log("rename:   ");
            console.log(this.editedValue);
            $('#renameItem .singleInputModalForm').modal('show');
        },
        onRenameSuccess: function () {
            var ref = this;
            ref.clearErrorCode();
            ref.dialogOperationInProgress = true;
            var obj = {
                parentUri: this.editedItem.data.parentUri,
                name: this.editedValue,
                type: this.editedItem.data.type
            };

            axios.put(this.editedItem.data.uri, obj)
                .then(function (response) {
                    if (response.status === 200) {
                        ref.errorCode = "";
                        ref.editedItem.data.name = ref.editedValue;
                        ref.editedItem.selected = false;
                        $("#renameItem .singleInputModalForm").modal('hide');
                    } else {
                        var errMsg = "Response on rename = " + ref.editedValue;
                        ref.makeError(errMsg, response.status);
                    }
                    ref.dialogOperationInProgress = false;
                })
                .catch(function (reason) {
                    var errMsg = "Response on rename = " + ref.editedValue;
                    ref.makeError(errMsg, reason.response.status);
                    ref.dialogOperationInProgress = false;
                });
        },
        onDelete: function () {
            if (this.selectedItemCount < 1) {
                console.log('No selected elements');
                return;
            }


            console.log("delete:   ");
            console.log(this.selectedItemCount);
            $('#deleteItems #confirmModalForm').modal('show');
        },
        onDeleteConfirm: function () {
            var ref = this;
            this.errorCode = "-1";
            var count = this.selectedItemCount;

            this.elements.forEach(function (item, index, object) {
                if (!item.selected) {
                    return;
                }
                axios.delete(item.data.uri)
                    .then(function (response) {
                        if (response.status === 200) {
                            object.splice(index, 1);
                        } else {
                            var errMsg = "Response on rename = " + ref.editedValue;
                            ref.makeError(errMsg, response.status);
                        }
                        count--;
                        if (count === 0) {
                            $("#deleteItems #confirmModalForm").modal('hide');
                        }
                    })
                    .catch(function (reason) {
                        var errMsg = "Response on rename = " + ref.editedValue;
                        ref.makeError(errMsg, reason.response.status);
                        count--;
                        if (count === 0) {
                            $("#deleteItems #confirmModalForm").modal('hide');
                        }
                    });
            });
        },
        onMove: function () {
            if (this.selectedItemCount < 1) {
                console.log('Moving failure');
                return;
            }

            console.log("moving:   ");
            console.log(this.selectedItemCount);
            this.editedValue = apiUrl('directory');
            // this.$refs.moveModalForm.currentUrl = ;
            this.$refs.moveModalForm.loadSubDirectories(apiUrl('directories'));
            $('#moveItems .moveModalForm').modal('show');
        },
        onMoveConfirm: function (parentUrl) {
            var targetParentUrl = parentUrl.replace('/api/', '/user/').replace('/directories/', '/drive/');
            var ref = this;
            ref.operationInProgress = true;
            var count = this.selectedItemCount;

            this.elements.forEach(function (item, index, object) {
                if (!item.selected) {
                    return;
                }
                if (targetParentUrl === item.data.parentUri) {
                    count--;
                    if (count === 0) {
                        $('#moveItems .moveModalForm').modal('hide');
                    }
                    return;
                }
                var obj = {
                    parentUri: targetParentUrl,
                    name: item.data.name,
                    type: item.data.type
                };
                axios.put(item.data.uri, obj)
                    .then(function (response) {
                        if (response.status === 200) {
                            object.splice(index, 1);
                        } else {
                            var errMsg = "Response on moving = " + item.data.uri;
                            ref.makeError(errMsg, response.status);
                        }
                        count--;
                        if (count === 0) {
                            $('#moveItems .moveModalForm').modal('hide');
                        }
                    })
                    .catch(function (reason) {
                        var errMsg = "Response on moving = " + item.data.uri;
                        ref.makeError(errMsg, reason.response.status);
                        count--;
                        if (count === 0) {
                            $('#moveItems .moveModalForm').modal('hide');
                        }
                    });
            });
        },
        onTweet: function () {
            this.editedValue = '';
            this.clearErrorCode();
            this.dialogOperationInProgress = false;
            $('#createTweetMessage .singleTextareaModalForm').modal('show');
        },
        onTweetSuccess: function () {
            var ref = this;

            console.log('Create tweet...\n');
            var paths = [];
            this.elements.forEach(function (item) {
                if (!item.selected || ref.isDirectory(item)) {
                    return;
                }

                paths.push(item.data.uri);
            });
            var sendObject = {
                'paths': paths,
                'message': ref.editedValue
            };
            ref.dialogOperationInProgress = true;

            axios.post(shortApiUrl('tweet'),
                JSON.stringify(sendObject),
                {headers: {"Content-Type": "application/json"}})
                .then(function (response) {
                    if (response.status === 200) {
                        $('#createTweetMessage .singleTextareaModalForm').modal('hide');
                        ref.showTweetPublishedForm(response.data)
                    } else {
                        ref.errorCode = response.status;
                        ref.error = response.data;
                    }
                    ref.dialogOperationInProgress = false;
                })
                .catch(function (reason) {
                    ref.errorCode = reason.response.status;
                    ref.error = reason.response.data;
                    ref.dialogOperationInProgress = false;
                });
        },
        selectAll: function (state) {
            this.elements.forEach(function (item) {
                item.selected = state;
            });
        },
        changeAllSelection: function () {
            this.selectAll(!this.isSelectAll);
        },
        selectDirectories: function () {
            this.filter('DIR')
                .forEach(function (item) {
                    item.selected = true;
                });
        },
        selectFiles: function () {
            this.filter('FILE')
                .forEach(function (item) {
                    item.selected = true;
                });
        },
        showTweetPublishedForm: function (tweetDTO) {
            this.errorCode = "";
            this.editedValue = tweetDTO['url'];
            this.selectAll(false);
            $('#tweetPublished .singleInputModalForm').modal('show');
        },
        hideTweetPublishedForm: function () {
            $('#tweetPublished .singleInputModalForm').modal('hide');
        },
        sizeMenuItemClass: function (countPerRow) {
            if (countPerRow !== this.size.countPerRow) {
                return 'hidden';
            } else {
                return '';
            }
        },
        sortOption: function (fieldName) {
            if (this.isSortedByField(fieldName)) {
                return '';
            } else {
                return 'hidden';
            }
        },
        sortingDirection: function () {
            this.sortOptions.changeOrder(this.sortOptions.fieldName);
        },
        redirectToItem: function (url) {
            window.location.href = url;
        }
    },
    created: function () {
        this.url = decodeURI(document.URL);
        this.loadDirectoryData();
        $('#tweetPublished .singleInputModalForm .btn-secondary').hide();
    }
});