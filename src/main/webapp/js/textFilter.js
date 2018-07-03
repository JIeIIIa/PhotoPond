var textFilter = Vue.component('text-filter', {
    props: {
        value: ""
    },
    data() {
        return {
            inputVal: this.value,
            inputVisible: false
        }
    },
    methods: {
        init() {
            this.inputVisible = !this.inputVisible;
            this.inputVal = '';
        }
    },
    watch: {
        inputVal(val) {
            this.$emit('input', val);
        }
    }
});
