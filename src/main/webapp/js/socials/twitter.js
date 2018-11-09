var twitter = Vue.component('twitter-accounts', {
    props: [],
    data() {
        return {
            accounts: []
        }
    },
    computed: {
        isShowInformation: function () {
            return this.accounts.length > 0;
        }
    },
    methods: {

    },
    created: function () {
        // alert("1");
    }
});