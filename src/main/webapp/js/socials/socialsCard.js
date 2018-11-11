var socialAccounts = Vue.component('social-accounts-card', {
    data() {
        return {
            showLoader: {
                facebook: false,
                twitter: false
            },
            errorMessageObject: {
                facebook: [],
                twitter: []
            },
            accounts: {
                facebook: [],
                twitter: []
            }
        }
    },
    computed: {
        isShowLoader: function () {
            var result = false;

            $.each(this.showLoader, function (index, value) {
                result |= value;
            });
            return result;
        }
    },
    methods: {
        loadAccounts(type) {
            console.log("Start loading facebook accounts");
            var ref = this;
            ref.showLoader[type] = true;
            axios.get('/private/' + type + '/accounts')
                .then(function (response) {
                    console.log("facebook accounts were gotten");
                    ref.accounts[type] = [];
                    if (response.status === 200) {
                        if (response.data !== "") {
                            ref.accounts[type].push(response.data);
                        }
                    } else {
                        console.log("facebook account loading error");
                        ref.addError(type, {'text': response.data, 'code': response.status});
                    }
                    ref.showLoader[type] = false;
                })
                .catch(function (reason) {
                    console.log("Loading facebook account have errors");
                    ref.addError(type, {'text': reason.response.data, 'code': reason.response.status});
                    ref.showLoader[type] = false;
                });
        },
        closeAlert(type) {
            if (type in this.errorMessageObject) {
                this.errorMessageObject[type] = [];
            }
        },
        addError(type, error) {
            if (type in this.errorMessageObject) {
                this.errorMessageObject[type] = [];
                this.errorMessageObject[type].push(error);
            }
        }
    },
    created: function () {
        this.loadAccounts('facebook');
        this.loadAccounts('twitter');
    }
});