var alertItem = Vue.component('alert-item', {
    props: ['messages', 'alertType'],
    methods: {
        close: function(key){
            this.$emit('alert-item-close', key);
        }
    }
});
