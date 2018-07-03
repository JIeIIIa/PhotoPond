var modalForm = Vue.component('user-modal-form', {
    props: ['user', 'errorMessage'],
    data() {
        return {
            savingInProgress: false,
            errorCode: ""
        }
    },
    methods: {
        saveData() {
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
        cancel() {
            $('#modalUserInfoForm').modal('hide');
        }
    },
    watch: {
        user(value) {
            this.errorCode = "";
        }
    },
    computed: {
        errorMessages() {
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
