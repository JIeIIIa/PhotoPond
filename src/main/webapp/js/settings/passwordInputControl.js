var passwordInput = Vue.component('password-input-control', {
   props: ['label', 'value', 'errorMsg', 'name'],
   methods: {
       close: function (name) {
           this.$emit('alert-item-close', name);
       }
   }
});