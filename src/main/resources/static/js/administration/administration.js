var loader = Vue.component('loader-component', {});

var administrationApp = new Vue({
    el: "#administrationApp",
    data: function() {
        return {
            activeItem: 2
        }
    },
    methods: {
        isMenuItemSelected: function(index){
            return this.activeItem === index;
        },
        menuItemClass: function(index) {
            if (this.isMenuItemSelected(index)) {
                return ['active', 'disabled'];
            } else {
                return ['bg-dark'];
            }
        },
        setActiveItem: function(index){
            this.activeItem = index;
        }

    },
    created: function () {
        var app = $("#administrationApp");
        if (app.data('start-item')) {
            this.activeItem = app.data('start-item');
        }
    }
});