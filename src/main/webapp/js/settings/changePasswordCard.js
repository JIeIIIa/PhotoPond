var loader = Vue.component('loader-component', {});

var changePassword = Vue.component('change-password-card', {
    props: [],
    data: function() {
        return {
            inputs: {
                oldPassword: {
                    value: '',
                    errorMsg: ''
                },
                password: {
                    value: '',
                    errorMsg: ''
                },
                passwordConfirmation: {
                    value: '',
                    errorMsg: ''
                }
            },
            showLoader: false,
            alert: {
                messages: [],
                type: ''
            }
        }
    },
    computed: {
        isPasswordNotConfirmed: function () {
            return this.inputs.password.value !== this.inputs.passwordConfirmation.value;
        },
        isChangeNotAllowed: function () {
            return this.isPasswordNotConfirmed || this.inputs.password.value.length < 5 || this.showLoader;
        }
    },
    methods: {
        clearItem: function (item) {
            item.value = '';
            item.errorMsg = '';
        },
        clearInputs: function () {
            for(var key in this.inputs) {
                if (!this.inputs.hasOwnProperty(key)) continue;
                this.clearItem(this.inputs[key]);
            }
        },
        resolveError: function(item) {
            if ('userInfoDTO.oldPassword' === item['objectName']) {
                this.inputs.oldPassword.errorMsg = item['message'];
            }
            if ('userInfoDTO.password' === item['objectName']) {
                this.inputs.password.errorMsg = item['message'];
            }
            if ('EqualPasswords.userInfoDTO' === item['objectName']) {
                this.inputs.passwordConfirmation.errorMsg = item['message'];
            }
        },
        resolveResponseErrors: function (errors) {
            var ref = this;
            if (!$.isArray(errors)) {
                console.log(errors);
                alert(errors);
                return;
            }
            errors.forEach(function (e) {
                ref.resolveError(e);
            })
        },
        responseWithError: function (response) {
            if (response.status === 422) {
                this.alert.messages.push({
                    text: response.data,
                    code: response.status
                });
                this.alert.type = "danger";
            } else {
                this.resolveResponseErrors(response.data);
            }
        },
        change: function () {
            var ref = this;
            var obj = {
                login: $('#login').text(),
                oldPassword: this.inputs.oldPassword.value,
                password: this.inputs.password.value,
                passwordConfirmation: this.inputs.passwordConfirmation.value
            };

            ref.showLoader = true;
            ref.clearErrors();
            axios.put(appendToUrl(location.protocol + '//' + location.host + location.pathname, "password"), obj)
                .then(function (response) {
                    if (response.status === 200) {
                        ref.alert.messages.push({text: response.data});
                        ref.alert.type = "success";
                        ref.clearInputs();
                    } else {
                        ref.responseWithError(response);
                    }
                    ref.showLoader = false;
                })
                .catch(function (reason) {
                    ref.responseWithError(reason.response);
                    ref.showLoader = false;
                });
        },
        clearErrors: function () {
            this.alert.messages = [];
            this.alert.type = '';
            for(var key in this.inputs) {
                if (!this.inputs.hasOwnProperty(key)) continue;
                this.inputs[key].errorMsg = '';
            }
        },
        closeAlert: function (key) {
            this.clearErrors();
        }
    }

});