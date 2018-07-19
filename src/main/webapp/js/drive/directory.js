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
            selectedMode: false,
            dialogOperationInProgress: false
        }
    },
    computed: {
        elementsWithUri() {
            return this.elements.map(function(item) {
                item.data.uri = item.data.parentUri + '/' + item.data.name;
                return item;
            })
        },
        sortedElements() {
            return this.elementsWithUri;
        },
        baseApiUrl() {
            var baseApiUrl = this.url.split('/');
            if (baseApiUrl.length > 5) {
                baseApiUrl[3] = 'api';
                baseApiUrl[5] = 'directory';
            }
            baseApiUrl = baseApiUrl.join('/');
            console.log('baseApiUrl:   ' + baseApiUrl);

            return baseApiUrl;
        },
        uploadFileApiUrl() {
            var result = this.baseApiUrl.split('/');
            if (result.length > 5) {
                result[5] = 'files';
            }

            return result.join('/');
        },
        childDirectoriesUrl() {
            var result = this.baseApiUrl.split('/');
            if (result.length > 5) {
                result[5] = 'directories';
            }

            return result.join('/');
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
        }
    },
    methods: {
        loadDirectoryData() {
            var ref = this;
            // ref.showLoader = true;
            axios.get(this.baseApiUrl)
                .then(function (response) {
                    response.data.forEach(function (item) {
                        ref.addElement(item);
                    });
                    ref.allUsers = response.data;

                    // ref.showLoader = false;
                })
                .catch(function (error) {
                    // ref.showLoader = false;
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

            axios.post(this.uploadFileApiUrl, formData, {
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
            var url = this.baseApiUrl;
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
                console.log('Renaming failure');
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

            this.elements.forEach(function(item, index, object) {
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
                        if(count === 0) {
                            $("#deleteItems #confirmModalForm").modal('hide');
                        }
                    })
                    .catch(function (reason) {
                        var errMsg = "Response on rename = " + ref.editedValue;
                        ref.makeError(errMsg, reason.response.status);
                        count--;
                        if(count === 0) {
                            $("#deleteItems #confirmModalForm").modal('hide');
                        }
                    });
            });
        },
        onMove(){
            if (this.selectedItemCount < 1) {
                console.log('Moving failure');
                return;
            }

            console.log("moving:   ");
            console.log(this.selectedItemCount);
            this.editedValue = this.baseApiUrl;
            // this.$refs.moveModalForm.currentUrl = ;
            this.$refs.moveModalForm.loadSubDirectories(this.childDirectoriesUrl);
            $('#moveItems .moveModalForm').modal('show');
        },
        onMoveConfirm(parentUrl) {
            var targetParentUrl = parentUrl.replace('/api/', '/user/').replace('/directories/', '/drive/');
            var ref = this;
            ref.operationInProgress = true;
            var count = this.selectedItemCount;

            this.elements.forEach(function(item, index, object) {
                if (!item.selected) {
                    return;
                }
                if (targetParentUrl === item.data.parentUri) {
                    count--;
                    if(count === 0) {
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
                        if(count === 0) {
                            $('#moveItems .moveModalForm').modal('hide');
                        }
                    })
                    .catch(function (reason) {
                        var errMsg = "Response on moving = " + item.data.uri;
                        ref.makeError(errMsg, reason.response.status);
                        count--;
                        if(count === 0) {
                            $('#moveItems .moveModalForm').modal('hide');
                        }
                    });
            });
        }
    },
    created: function () {
        this.url = document.URL;
        this.loadDirectoryData();
    }
});