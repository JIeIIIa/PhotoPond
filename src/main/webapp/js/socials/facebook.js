var fb = Vue.component('facebook-accounts', {
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
            this.$emit('close-alert', 'facebook');
        }
    },
    mounted: function() {
        var fbErrorMessage = $("#fbErrorMessage");
        if (fbErrorMessage.length !== 0) {
            this.$emit('add-error', 'facebook', {'text': fbErrorMessage.text()});
        }
    }
});