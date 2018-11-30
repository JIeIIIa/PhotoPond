var alertItem = Vue.component('alert-item', {
    props: ['messages', 'alertType'],
    methods: {
        close: function (key) {
            this.$emit('alert-item-close', key);
        }
    }
});

var smallAlertItem = Vue.component('small-alert-item', {
    props: ['alertMessage', 'name'],
    methods: {
        close: function (name) {
            this.$emit('alert-item-close', name);
        }
    }
});