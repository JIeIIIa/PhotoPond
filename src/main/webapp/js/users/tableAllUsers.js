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
        isAsc: function() {
            return this.sortOptions.isAscend()
        },
        isDesc: function() {
            return !this.sortOptions.isAscend()
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
