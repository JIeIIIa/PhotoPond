var slider = Vue.component('slider', {
        props: ['images', 'index'],
        data: function () {
            return {
                transitionGroup: ''
            }
        },

        computed: {
            isComponentVisible: function() {
                return 0 <= this.index && this.index < this.images.length;
            },
            isSliderIconNotVisible: function () {
                return this.images.length <= 1;
            }
        }
        ,
        methods: {
            close: function () {
                this.$emit('close');
            },
            indexIncrement: function () {
                this.transitionGroup = 'slider-move-left';
                this.$emit('index-increment');
            },
            indexDecrement: function () {
                this.transitionGroup = 'slider-move-right';
                this.$emit('index-decrement');
            }
        },
        mounted: function () {
            var ref = this;
            window.addEventListener('keyup', function (event) {
                if (ref.index === -1) {
                    return;
                }
                if (event.key === 'Escape') {
                    ref.close();
                } else if (event.key === 'ArrowRight') {
                    ref.indexIncrement();
                } else if (event.key === 'ArrowLeft') {
                    ref.indexDecrement();
                }
            });
        }
    }
);

