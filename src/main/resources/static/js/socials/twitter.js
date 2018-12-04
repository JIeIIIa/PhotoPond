var twitter = Vue.component('twitter-accounts', {
    props: ["accounts", "errorMessages"],
    computed: {
        isShowInformation: function () {
            return this.accounts.length > 0;
        },
        isErrorsVisible: function () {
            return !$.isEmptyObject(this.errorMessages);
        }
    },
    methods: {
        closeAlert: function() {
            this.$emit('close-alert', 'twitter');
        }
    },
    mounted: function() {
        var twitterErrorMessage = $("#twitterErrorMessage");
        if (twitterErrorMessage.length !== 0) {
            this.$emit('add-error', 'twitter', {'text': twitterErrorMessage.text()});
        }
    }
});