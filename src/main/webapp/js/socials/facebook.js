var fb = Vue.component('facebook-accounts', {
    props: ["accounts", "errorMessages"],
    data() {
        return {
            // accounts: []
        }
    },
    computed: {
        isShowInformation: function () {
            return this.accounts.length > 0;
        },
        isErrorsVisible: function () {
            return !$.isEmptyObject(this.errorMessages);
        }
    },
    methods: {
        closeAlert() {
            this.$emit('close-alert', 'facebook');
        }
    },
    mounted() {
        var fbErrorMessage = $("#fbErrorMessage");
        if (fbErrorMessage.length !== 0) {
            this.$emit('add-error', 'facebook', {'text': fbErrorMessage.text()});
        }
    }
});