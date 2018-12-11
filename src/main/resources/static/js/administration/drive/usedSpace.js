var usedSpace = Vue.component('used-space', {
    data: function () {
        return {
            statisticsDTO: [],
            showLoader: false,
            filterTemplate: "",
            errorCode: "",
            message: ""
        }
    },
    computed: {
        filteredData: function () {
            if ("" === this.filterTemplate) {
                return this.statisticsDTO;
            }
            var template = this.filterTemplate.toLocaleLowerCase();
            return this.statisticsDTO.filter(function (a) {
                return String(a['login']).toLocaleLowerCase().indexOf(template) > -1;
            })
        }
    },

    methods: {
        loadInformation: function () {
            var ref = this;
            ref.showLoader = true;
            axios.get("/administration/drive/statistics")
                .then(function (response) {
                    ref.statisticsDTO = response.data;
                    ref.showLoader = false;
                })
                .catch(function (error) {
                    ref.showLoader = false;
                })
        }
    },
    created: function () {
        this.loadInformation();
    }
});