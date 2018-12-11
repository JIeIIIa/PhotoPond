var users = Vue.component('users-table', {
    props: ['usersList'],
    data: function() {
        return {
            sortOptions: new SortOptions("id")
        }
    },
    computed: {
        sortedUsersList: function() {
            return this.usersList
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        textArrowClass: function () {
            if(this.sortOptions.isAscend()) {
                return 'fa-arrow-up';
            } else {
                return 'fa-arrow-down';
            }
        }
    },
    methods: {
        sortBy: function(col) {
            this.sortOptions.changeOrder(col);
        },
        isSortedByField: function(fieldName) {
            return this.sortOptions.fieldName === fieldName;
        }
    }
});
