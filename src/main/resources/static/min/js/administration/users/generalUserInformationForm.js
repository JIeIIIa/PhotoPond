var modalForm=Vue.component("user-modal-form",{props:["user","errorMessage"],data:function(){return{savingInProgress:!1,errorCode:""}},methods:{saveData:function(){var a=this;a.savingInProgress=!0;axios.post(adminUrlTemplate.users.userById+this.user.id,this.user).then(function(b){200===b.status?(console.log("User was updated"),a.$emit("done-edit",b.data)):a.errorCode=b.status;a.savingInProgress=!1})},cancel:function(){$("#modalUserInfoForm").modal("hide")}},watch:{user:function(a){this.errorCode=
""}},computed:{errorMessages:function(){var a=[];""!==this.errorCode&&a.push({text:this.errorMessage,code:this.errorCode});return a}}});