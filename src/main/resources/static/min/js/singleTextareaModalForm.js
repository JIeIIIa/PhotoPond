var singleTextareaModalForm=Vue.component("single-textarea-modal-form",{props:"operationInProgress errorMessage errorCode customHeader message value successButtonTitle".split(" "),data:function(){return{errorVisible:!1,maxLength:240}},methods:{cancel:function(){$(".singleInputModalForm").modal("hide")},success:function(){this.$emit("success",this.value)},closeAlert:function(){this.$emit("clear-error-code")}},computed:{errorMessageObject:function(){var a=[];""!==this.errorCode&&a.push({text:this.errorMessage,
code:this.errorCode});return a},isInputDisabled:function(){return""===this.value},charsRemaining:function(){return this.maxLength-this.value.length}}});