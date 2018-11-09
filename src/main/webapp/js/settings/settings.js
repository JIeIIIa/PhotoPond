var userSettings = new Vue({
    el: "#settings",
    data() {
        return {
            activeItem: 2
        }
    },
    methods: {
        isMenuItemSelected(index){
            return this.activeItem === index;
        },
        setActiveItem(index){
            this.activeItem = index;
        }

    },
    created: function () {

        var settings = $("#settings");
        if (settings.data('start-item')) {
            this.activeItem = settings.data('start-item');
        } else {
            this.activeItem = 3;
        }

    }

});