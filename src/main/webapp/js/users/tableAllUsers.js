var users = Vue.component('users-table', {
    props: ['usersList'],
    data() {
        return {
            sortOptions: new SortOptions("id")
        }
    },
    computed: {
        sortedUsersList() {
            return this.usersList
                .sort(dynamicSort(this.sortOptions.fieldName, this.sortOptions.isAscend()));
        },
        isAsc() {
            return this.sortOptions.isAscend()
        },
        isDesc() {
            return !this.sortOptions.isAscend()
        }
    },
    methods: {
        sortBy(col) {
            this.sortOptions.changeOrder(col);
        },
        isSortedByField(fieldName) {
            return this.sortOptions.fieldName === fieldName;
        }

    }
});
