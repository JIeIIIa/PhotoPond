var slider = Vue.component('slider', {
        props: ['images', 'index'],
        data() {
            return {
                transitionGroup: ''
            }
        },

        computed: {
            visible() {
                return 0 <= this.index && this.index < this.images.length;
            },
            nextEnable() {
                return this.index < this.images.length - 1;
            }
            ,
            prevEnable() {
                return 0 < this.index;
            },
            trGr() {
                return this.transitionGroup;
            }
        }
        ,
        methods: {
            close() {
                this.$emit('close');
            },
            indexIncrement() {
                this.transitionGroup = 'slider-move-left';
                if (this.nextEnable) {
                    this.$emit('index-increment');
                }
            },
            indexDecrement() {
                this.transitionGroup = 'slider-move-right';
                if (this.prevEnable) {
                    this.$emit('index-decrement');
                }
            }
        },
        mounted() {
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

