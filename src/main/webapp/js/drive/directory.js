var loader = Vue.component('loader-component', {});

var app = new Vue({
    el: '#directoryApp',
    data() {
        return {
            url: '',
            elements: [],
            message: '',
            errorCode: '',
            editedValue: '',
            editedItem: '',
            dialogOperationInProgress: false,
            showLoader: false,
            size: {
                countPerRow: 2,
                suffix: [12, 6, 4, 3, 2, 1]
            },
            imageIndex: -1,
            sortOptions: new SortOptions("data.name")
        }
    },
    computed: {
        elementsWithUri() {
            return this.elements.map(function (item) {
                item.data.uri = item.data.parentUri + '/' + item.data.name;
                return item;
            })
        },
        sortedDirectories() {
            return this
                .filter('DIR')
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        sortedFiles() {
            return this
                .filter('FILE')
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        parentDirectories() {
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
        selectedItemCount() {
            var count = 0;
            this.elements.forEach(function (item) {
                if (item.selected) {
                    count++;
                }
            });
            return count;
        },
        selectedFileCount() {
            var count = 0;
            var ref = this;
            this.elements.forEach(function (item) {
                if (item.selected && ref.isFile(item)) {
                    count++;
                }
            });
            return count;
        },
        hasDirectory() {
            for (var i = 0; i < this.elements.length; i++) {
                if (this.isDirectory(this.elements[i])) {
                    return true;
                }
            }
            return false;
        },
        hasFile() {
            for (var i = 0; i < this.elements.length; i++) {
                if (this.isFile(this.elements[i])) {
                    return true;
                }
            }
            return false;
        },
        colSizeClasses() {
            return ['col-' + this.size.suffix[this.size.countPerRow - 1],
                'col-sm-' + this.size.suffix[this.size.countPerRow],
                'col-md-' + this.size.suffix[this.size.countPerRow + 1],
                'col-xl-' + this.size.suffix[this.size.countPerRow + 2]];
        },
        heightSizeClasses() {
            return ['height-vw-' + this.size.suffix[this.size.countPerRow - 1],
                'height-vw-sm-' + this.size.suffix[this.size.countPerRow],
                'height-vw-md-' + this.size.suffix[this.size.countPerRow + 1],
                'height-vw-xl-' + this.size.suffix[this.size.countPerRow + 2]];
        },
        images() {

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
        isSelectAll() {
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
        dateConvert(unixTimeStamp) {
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
        filter(type) {
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
        pageNotFound() {
            var redirectedUrl = '/page-not-found';
            var form = $('<form action="' + redirectedUrl + '" method="post" hidden="hidden">' +
                '<input type="text" name="url" value="' + window.location.href + '" />' +
                '</form>');
            $('body').append(form);
            form.submit();
        },
        loadDirectoryData() {
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
                    ref.pageNotFound();
                })
        },
        addElement(data) {
            data.uri = data.parentUri + '/' + data.name;
            this.elements.push({
                data: data,
                selected: false
            })
        },
        isDirectory(item) {
            return item.data.type === 'DIR';
        },
        isFile(item) {
            return item.data.type === 'FILE';
        },
        clearErrorCode() {
            this.errorCode = '';
        },
        makeError(errMsg, status) {
            console.log('code ' + status + ': ' + errMsg);
            this.errorCode = status;
        },
        onFileUpload() {
            console.log('start files upload');
            this.$refs.files.click();
        },
        onFileUploadFinish() {
            console.log('Files upload finish\n');
            var uploadedFiles = this.$refs.files.files;
            var formData = new FormData();
            for (var i = 0; i < uploadedFiles.length; i++) {
                formData.append('files', uploadedFiles[i]);
            }

            var ref = this;

            axios.post(apiUrl('files'), formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(function (response) {
                if (response.status === 200) {
                    response.data.forEach(function (item) {
                        ref.addElement(item);
                    });
                } else {
                    console.log('Error in uploading files (status = ' + response.status + ')');
                }
            }).catch(function (reason) {
                console.log('Error in uploading files (status = ' + reason.response.status + ')');
            })
        },
        onCreateDirectory() {
            this.editedValue = '';
            this.clearErrorCode();
            this.dialogOperationInProgress = false;
            $('#createDirectory .singleInputModalForm').modal('show');
        },
        onCreateDirectorySuccess(directoryName) {
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
        onRename() {
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
        onRenameSuccess() {
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
        onDelete() {
            if (this.selectedItemCount < 1) {
                console.log('No selected elements');
                return;
            }


            console.log("delete:   ");
            console.log(this.selectedItemCount);
            $('#deleteItems #confirmModalForm').modal('show');
        },
        onDeleteConfirm() {
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
        onMove() {
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
        onMoveConfirm(parentUrl) {
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
        onTweet() {
            this.editedValue = '';
            this.clearErrorCode();
            this.dialogOperationInProgress = false;
            $('#createTweetMessage .singleTextareaModalForm').modal('show');
        },
        onTweetSuccess() {
            var ref = this;

            console.log('Create tweet...\n');
            var paths = [];
            this.elements.forEach(function (item, index, object) {
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
        selectAll(state) {
            this.elements.forEach(function (item) {
                item.selected = state;
            });
        },
        changeAllSelection() {
            this.selectAll(!this.isSelectAll);
        },
        selectDirectories() {
            this.filter('DIR')
                .forEach(function (item) {
                    item.selected = true;
                });
        },
        selectFiles() {
            this.filter('FILE')
                .forEach(function (item) {
                    item.selected = true;
                });
        },
        showTweetPublishedForm(tweetDTO) {
            this.errorCode = "";
            this.editedValue = tweetDTO['url'];
            this.selectAll(false);
            $('#tweetPublished .singleInputModalForm').modal('show');
        },
        hideTweetPublishedForm() {
            $('#tweetPublished .singleInputModalForm').modal('hide');
        },
        sizeMenuItemClass(countPerRow) {
            if (countPerRow !== this.size.countPerRow) {
                return 'hidden';
            } else {
                return '';
            }
        },
        sortOption(fieldName) {
            if (this.isSortedByField(fieldName)) {
                return '';
            } else {
                return 'hidden';
            }
        },
        sortingDirection() {
            this.sortOptions.changeOrder(this.sortOptions.fieldName);
        }
    },
    created: function () {
        this.url = decodeURI(document.URL);
        this.loadDirectoryData();
        $('#tweetPublished .singleInputModalForm .btn-secondary').hide();
    }
});