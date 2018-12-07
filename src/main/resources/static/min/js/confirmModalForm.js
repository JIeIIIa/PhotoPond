var confirmModalForm=Vue.component("confirm-modal-form",{props:["customHeader","errorMessage","errorCode","message","value"],data:function(){return{operationInProgress:!1,errorVisible:!1}},methods:{cancel:function(){$("#confirmModalForm").modal("hide")},confirm:function(){this.$emit("confirm",this.value)},closeAlert:function(){this.$emit("clear-error-code")}},watch:{errorCode:function(a){this.operationInProgress=!this.operationInProgress&&""!==this.errorCode}},computed:{errorMessageObject:function(){var a=
[];""!==this.errorCode&&a.push({text:this.errorMessage,code:this.errorCode});return a}}});