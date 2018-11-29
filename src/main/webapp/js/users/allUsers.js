var loader = Vue.component('loader-component', {});

var vm = new Vue({
    el: '#usersApp',
    data: function() {
        return {
            allUsers: [],
            editedUser: {},
            showLoader: false,
            filterTemplate: "",
            errorCode: "",
            message: ""
        }
    },
    computed: {
        filteredData: function() {
            if ("" === this.filterTemplate) {
                return this.allUsers;
            }
            var template = this.filterTemplate.toLocaleLowerCase();
            return this.allUsers.filter(function (a) {
                return String(a['login']).toLocaleLowerCase().indexOf(template) > -1;
            })
        }
    },

    methods: {
        loadAllUsers: function() {
            var ref = this;
            ref.showLoader = true;
            axios.get(urlTemplate.admin.allUsers)
                .then(function (response) {
                    ref.allUsers = response.data;
                    ref.showLoader = false;
                })
                .catch(function (error) {
                    ref.showLoader = false;
                })
        },
        userByIndex: function(user) {
            return this.allUsers.findIndex(function (u) {
                return u.id === user.id;
            });
        },
        onDelete: function(key) {
            this.editedUser = this.allUsers[key];
            this.errorCode = "";
            this.message = "<div class='row'>" +
                "<div class='col-4'>id:</div><div class='col-8'>" + this.editedUser['id'] + "</div>" +
                "<div class='col-4'>Login:</div><div class='col-8'>" + this.editedUser['login'] + "</div>" +
                "</div>";
            $("#confirmModalForm").modal('show');
        },
        onDeleteConfirm: function(user) {
            this.errorCode = "-1";
            console.log(user);
            var index = this.userByIndex(user);
            var ref = this;
            axios.delete(urlTemplate.admin.userById + user.id)
                .then(function (response) {
                    if (response.status === 200) {
                        ref.errorCode = "";
                        $("#confirmModalForm").modal('hide');
                        ref.allUsers.splice(index, 1);
                    } else {
                        ref.errorCode = response.status;
                        var errMsg = "Response on delete id = " + user.id + "\n" + response.status;
                        console.log(errMsg);
                        ref.errorCode = response.status;
                    }
                });
        },
        editUser: function(user) {
            this.editedUser = _.cloneDeep(user);
            this.showModal = true;
            $("#modalUserInfoForm").modal('show');
        },
        doneEdit: function(user) {
            $("#modalUserInfoForm").modal('hide');
            var index = this.userByIndex(user);
            this.allUsers.splice(index, 1, user);
            this.showModal = false;
        }
    },
    created: function () {
        this.loadAllUsers();
    }
});