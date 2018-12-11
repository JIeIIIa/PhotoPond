function splitApiUrl(serviceName) {
    var apiUrl = document.URL.split('/');
    if (apiUrl.length > 5) {
        apiUrl[3] = 'api';
        apiUrl[5] = serviceName;
    }
    return apiUrl;
}

function apiUrl(serviceName) {
    var apiUrl = this.splitApiUrl(serviceName).join('/');
    console.log('apiUrl:   ' + apiUrl);

    return apiUrl;
}

function shortApiUrl(serviceName) {
    var shortUrl = this.splitApiUrl(serviceName);
    shortUrl.length = 6;
    shortUrl = shortUrl.join('/');
    console.log('shortUrl:   ' + shortUrl);

    return shortUrl;
}

function SortOptions(fieldName) {
    this.fieldName = fieldName;
    this.ascend = true;

    this.changeOrder = function (fieldName) {
        if (fieldName !== this.fieldName) {
            this.fieldName = fieldName;
            this.ascend = true;
        } else {
            this.ascend = !this.ascend;
        }
    };

    this.isAscend = function () {
        return this.ascend;
    }
}

function dynamicSort(property, direction) {
    var sortOrder = direction ? 1 : -1;

    return function (a, b) {
        if (_.has(a, property) && _.has(b, property)) {
            var first = _.get(a, property);
            var second = _.get(b, property);
            var result;
            if ((typeof first === "string") && (typeof second === "string")) {
                result = first.toLowerCase().localeCompare(second.toLowerCase());
            } else {
                result = (first < second) ? -1 : (first > second) ? 1 : 0;
            }
            return result * sortOrder;
        } else {
            return 0;
        }
    }
}

function appendToUrl(url, part) {
    if (url.endsWith('/')) {
        return url + part;
    } else {
        return url + '/' + part;
    }
}

/**
 * Enable Vuejs managing for Bootstrap tooltips for elements that have 'v-tooltip' attribute
 */
Vue.directive("tooltip", {
    bind: function (el) {
        $(el).tooltip({trigger: "hover", 'delay': {show: 1000, hide: 100}});
    }
});

/**
 * Enable Bootstrap tooltips for elements that have 'v-tooltip' attribute
 */
function enableTooltip() {
    $('[v-tooltip]').tooltip({trigger: "hover", 'delay': {show: 1000, hide: 100}});
}

/**
 * Add slide animation to Bootstrap dropdown elements
 */
function dropdownMenuSlideAnimation() {
    var dropdown = '.dropdown';
    $(dropdown).on('show.bs.dropdown', function (e) {
        $(this).find('.dropdown-menu').first().stop(true, true).slideDown(300);
    });

    $(dropdown).on('hide.bs.dropdown', function (e) {
        $(this).find('.dropdown-menu').first().stop(true, true).slideUp(200);
    });
}

/**
 * The ready() method specifies what happens when a ready event occurs.
 * The ready event occurs when the DOM (document object model) has been loaded.
 */
$(document).ready(function () {
    dropdownMenuSlideAnimation();
    enableTooltip();
});