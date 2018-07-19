var moveModalForm = Vue.component('move-modal-form', {
    props: ['errorMessage', 'customHeader', 'message', 'operationInProgress'],
    data() {
        return {
            currentUrl: '',
            content: {},
            subDirectories: [],
            loaderVisible: false,
            errorCode: ''
        }
    },
    watch: {
        content: function(val) {
            this.currentUrl = this.makePath(val.current);
            if (jQuery.isEmptyObject(val.current)) {
                this.currentUrl = '';
            }
            this.currentUrl = val.current.parentUri;
            if (!jQuery.isEmptyObject(val.current.name)) {
                this.currentUrl += '/' + val.current.name;
            }

        }
    },
    computed: {
        errorMessageObject() {
            var res = [];
            if(this.errorCode !== "") {
                res.push({
                    text: this.errorMessage,
                    code: this.errorCode
                });
            }

            return res;
        },
        subDirectoriesWithUri() {
            var list = [];
            if (!jQuery.isEmptyObject(this.content.parent)) {
                var item = this.content.parent;
                item.uri = item.parentUri;
                item.name = '..';
                list.push(item);
            }
            if (!jQuery.isEmptyObject(this.content.childDirectories)) {
                this.content.childDirectories.forEach(function (item) {
                    item.uri = item.parentUri + '/' + item.name;
                    list.push(item);
                })
            }

            return list;
        },
        isLoaderVisible() {
            return this.operationInProgress || this.loaderVisible;
        }
    },
    methods: {
        makePath(item) {
            if (jQuery.isEmptyObject(item)) {
                return '';
            }
            var uri = item.parentUri.replace(/\/api\/.+\/directories/, '..');
            if (!jQuery.isEmptyObject(item.name)) {
                uri += '/' + item.name;
            }

            return uri;
        },
        closeAlert: function() {
            this.errorCode = '';
        },
        loadSubDirectories(url) {
            var ref = this;
            ref.loaderVisible = true;
            this.errorCode = '';
            axios.get(url)
                .then(function (response) {
                    if(response.status === 200) {
                        ref.content = response.data;
                    } else {
                        ref.errorCode = response.status;
                    }
                    ref.loaderVisible = false;
                })
                .catch(function (reason) {
                    ref.errorCode = reason.response.status;
                    ref.loaderVisible = false;
                })
        },
        changeDirectory: function (item) {
            this.loadSubDirectories(item.uri);
        },
        cancel: function () {
            $('.moveModalForm').modal('hide');
        },
        confirm: function () {
            this.$emit('move-confirm', this.currentUrl);
        }
    }
});