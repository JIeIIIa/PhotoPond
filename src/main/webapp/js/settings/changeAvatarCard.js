var changeAvatar = Vue.component('change-avatar-card', {
    props: [],
    data() {
        return {
            showLoader: false,
            alert: {
                messages: [],
                type: ''
            },
            imageData: "",
            imageFile: ""
        }
    },
    computed: {
        hasOldData: function () {
            return this.imageData.length === 0;
        }
    },
    methods: {
        clearItem: function (item) {
            item.value = '';
            item.errorMsg = '';
        },
        resolveError(item) {

        },

        resolveResponseErrors: function (errors) {
            var ref = this;
            if (!$.isArray(errors)) {
                console.log(errors);
                alert(errors);
                return;
            }
            errors.forEach(function (e) {
                ref.resolveError(e);
            })
        },
        responseWithError: function (response) {
            if (response.status === 422) {
                this.alert.messages.push({
                    text: response.data,
                    code: response.status
                });
                this.alert.type = "danger";
            } else {
                this.resolveResponseErrors(response.data);
            }
        },
        change: function () {
            var ref = this;
            var obj = {};

            ref.showLoader = true;
            ref.clearErrors();
            axios.put(appendToUrl(location.protocol + '//' + location.host + location.pathname, "password"), obj)
                .then(function (response) {
                    if (response.status === 200) {
                        ref.alert.messages.push({text: response.data});
                        ref.alert.type = "success";
                        ref.clearInputs();
                    } else {
                        ref.responseWithError(response);
                    }
                    ref.showLoader = false;
                })
                .catch(function (reason) {
                    ref.responseWithError(reason.response);
                    ref.showLoader = false;
                });
        },
        clearErrors: function () {
            this.alert.messages = [];
            this.alert.type = '';
        },
        closeAlert: function (key) {
            this.clearErrors();
        },
        validateImage: function (file) {
            var fileType = file["type"];
            var ValidImageTypes = ["image/gif", "image/jpeg", "image/png"];
            return ($.inArray(fileType, ValidImageTypes) >= 0);
        },
        previewImage: function (event) {
            // Reference to the DOM input element
            var input = event.target;
            // Ensure that you have a file before attempting to read it
            if (input.files && input.files[0]) {
                var ref = this;
                // create a new FileReader to read this image and convert to base64 format
                if (!this.validateImage(input.files[0])) {
                    ref.clearErrors();
                    ref.alert.messages.push({
                        text: $("#choosePictureError").text().trim()
                    });
                    ref.alert.type = "danger";
                    return;
                }
                var reader = new FileReader();

                // Define a callback function to run, when FileReader finishes its job
                reader.onload = function (e) {
                    // Read image as base64 and set to imageData
                    ref.imageData = e.target.result;
                };
                // Start the reader job - read file as a data url (base64 format)
                reader.readAsDataURL(input.files[0]);
                ref.imageFile = input.files[0];
            }
        },
        onChoosePic: function () {
            $('#avatarImg').click();
        },
        reloadAvatars: function () {
            var imgs = $("img[src$='avatar']");
            var src = imgs.attr('src');
            imgs.attr('src', '');
            imgs.attr('src', src);
        },
        updateAvatar: function () {
            console.log('Sending the file to update the avatar...\n');
            this.clearErrors();

            var formData = new FormData();
            formData.append('avatar', this.imageFile);

            var ref = this;
            var newAvatar = $("#changeAvatar");
            var changeAvatarUrl = newAvatar.data('change-avatar-url');
            ref.showLoader = true;
            axios.post(changeAvatarUrl, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(function (response) {
                if (response.status === 200) {
                    ref.alert.messages.push({text: response.data});
                    ref.alert.type = "success";
                    ref.imageData = "";
                    ref.imageFile = "";
                    ref.reloadAvatars();
                } else {
                    console.log('Error uploading avatar (status = ' + response.status + ')');
                    ref.alert.messages.push({text: response.data});
                    ref.alert.type = "danger";
                }
                ref.showLoader = false;
            }).catch(function (reason) {
                if (!jQuery.isEmptyObject(reason.response) && !jQuery.isEmptyObject(reason.response.status)) {
                    console.log('Error uploading avatar (status = ' + reason.response.status + ')');
                    ref.alert.messages.push({text: reason.response.data});
                    ref.alert.type = "danger";
                }
                ref.showLoader = false;
            })
        },
        deleteAvatar: function () {
            console.log('Set the default avatar.\n');
            this.clearErrors();

            var ref = this;
            var newAvatar = $("#changeAvatar");
            var changeAvatarUrl = newAvatar.data('change-avatar-url');
            ref.showLoader = true;
            axios.delete(
                changeAvatarUrl
            ).then(function (response) {
                if (response.status === 200) {
                    ref.alert.messages.push({text: response.data});
                    ref.alert.type = "success";
                    ref.imageData = "";
                    ref.imageFile = "";
                    ref.reloadAvatars();
                } else {
                    console.log('Error deleting avatar (status = ' + response.status + ')');
                    ref.alert.messages.push({text: response.data});
                    ref.alert.type = "danger";
                }
                ref.showLoader = false;
            }).catch(function (reason) {
                if (!jQuery.isEmptyObject(reason.response) && !jQuery.isEmptyObject(reason.response.status)) {
                    console.log('Error deleting avatar (status = ' + reason.response.status + ')');
                    ref.alert.messages.push({text: reason.response.data});
                    ref.alert.type = "danger";
                }
                ref.showLoader = false;
            })
        }
    }

});