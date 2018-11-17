var urlTemplate = {
    admin: {
        allUsers: "/administration/users",
        userById: "/administration/user/"
    }
};

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

Vue.directive("tooltip", {
    bind: function (el) {
        $(el).tooltip({trigger: "hover", 'delay': {show: 1000, hide: 100}})
    }
});