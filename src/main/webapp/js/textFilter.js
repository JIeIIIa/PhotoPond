var textFilter = Vue.component('text-filter', {
    props: {
        value: ""
    },
    data: function() {
        return {
            inputVal: this.value,
            inputVisible: false
        }
    },
    methods: {
        init: function() {
            this.inputVisible = !this.inputVisible;
            this.inputVal = '';
        }
    },
    watch: {
        inputVal: function(val) {
            this.$emit('input', val);
        }
    }
});
