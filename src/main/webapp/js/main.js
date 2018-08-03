var urlTemplate = {
    admin: {
        allUsers: "/administration/users",
        userById: "/administration/user/"
    }
};

function SortOptions(fieldName){
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

    this.isAscend = function() {
        return this.ascend;
    }
}

function dynamicSort(property, direction) {
    var sortOrder = direction ? 1 : -1;

    return function (a,b) {
        var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
        return result * sortOrder;
    }
}

function appendToUrl(url, part) {
    if (url.endsWith('/')) {
        return url + part;
    } else {
        return url + '/' + part;
    }
}