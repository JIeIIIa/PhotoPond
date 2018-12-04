var confirmModalForm = Vue.component('confirm-modal-form', {
    props: ['customHeader', 'errorMessage', 'errorCode', 'message', 'value'],
    data: function() {
        return {
            operationInProgress: false,
            errorVisible: false
        }
    },
    methods: {
        cancel: function () {
            $('#confirmModalForm').modal('hide');
        },
        confirm: function () {
            this.$emit('confirm', this.value);
        },
        closeAlert: function() {
            this.$emit('clear-error-code')
        }
    },
    watch: {
        errorCode: function (newValue) {
            this.operationInProgress = (!this.operationInProgress) && (this.errorCode !== "");
        }
    },
    computed: {
        errorMessageObject: function() {
            var res = [];
            if(this.errorCode !== "") {
                res.push({
                    text: this.errorMessage,
                    code: this.errorCode
                });
            }
            return res;
        }
    }

});