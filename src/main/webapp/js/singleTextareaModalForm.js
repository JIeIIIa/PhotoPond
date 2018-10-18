var singleTextareaModalForm = Vue.component('single-textarea-modal-form', {
    props: ['operationInProgress',
        'errorMessage', 'errorCode',
        'customHeader', 'message', 'value', 'successButtonTitle'],
    data() {
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
        errorMessageObject() {
            var res = [];
            if (this.errorCode !== "") {
                res.push({
                    text: this.errorMessage,
                    code: this.errorCode
                });
            }

            return res;
        },
        isInputDisabled() {
            return this.value === "";
        },
        charsRemaining(){
            return this.maxLength - this.value.length;
        }
    }

});