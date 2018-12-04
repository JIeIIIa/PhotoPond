var singleTextareaModalForm = Vue.component('single-textarea-modal-form', {
    props: ['operationInProgress',
        'errorMessage', 'errorCode',
        'customHeader', 'message', 'value', 'successButtonTitle'],
    data: function() {
        return {
            errorVisible: false,
            maxLength: 240
        }
    },
    methods: {
        cancel: function () {
            $('.singleInputModalForm').modal('hide');
        },
        success: function () {
            this.$emit('success', this.value);
        },
        closeAlert: function () {
            this.$emit('clear-error-code')
        }
    },
    computed: {
        errorMessageObject: function() {
            var res = [];
            if (this.errorCode !== "") {
                res.push({
                    text: this.errorMessage,
                    code: this.errorCode
                });
            }

            return res;
        },
        isInputDisabled: function() {
            return this.value === "";
        },
        charsRemaining: function(){
            return this.maxLength - this.value.length;
        }
    }
});