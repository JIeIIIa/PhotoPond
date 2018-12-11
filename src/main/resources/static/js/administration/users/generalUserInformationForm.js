var modalForm = Vue.component('user-modal-form', {
    props: ['user', 'errorMessage'],
    data: function() {
        return {
            savingInProgress: false,
            errorCode: ""
        }
    },
    methods: {
        saveData: function() {
            var ref = this;
            ref.savingInProgress = true;
            axios.post(urlTemplate.admin.userById + this.user.id, this.user)
                .then(function (response) {
                    if (response.status === 200) {
                        console.log("User was updated");
                        ref.$emit('done-edit', response.data)
                    } else {
                        ref.errorCode = response.status;
                    }
                    ref.savingInProgress = false;
                });
        },
        cancel: function() {
            $('#modalUserInfoForm').modal('hide');
        }
    },
    watch: {
        user: function(value) {
            this.errorCode = "";
        }
    },
    computed: {
        errorMessages: function() {
            var res = [];
            if (this.errorCode !== "") {
                res.push({
                    text: this.errorMessage,
                    code: this.errorCode
                })
            }
            return res;
        }
    }
});
