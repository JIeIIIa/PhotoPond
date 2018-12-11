var loader=Vue.component("loader-component",{}),app=new Vue({el:"#directoryApp",data:function(){return{url:"",elements:[],message:"",errorCode:"",editedValue:"",editedItem:"",dialogOperationInProgress:!1,showLoader:!1,size:{countPerRow:2,suffix:[12,6,4,3,2,1]},imageIndex:-1,sortOptions:new SortOptions("data.name")}},computed:{elementsWithUri:function(){return this.elements.map(function(a){a.data.uri=a.data.parentUri+"/"+a.data.name;return a})},sortedDirectories:function(){return this.filter("DIR").sort(dynamicSort(this.sortOptions.fieldName,
this.sortOptions.isAscend()))},sortedFiles:function(){return this.filter("FILE").sort(dynamicSort(this.sortOptions.fieldName,this.sortOptions.isAscend()))},parentDirectories:function(){var a=this.url.split("/");0<a.length&&""===a[a.length-1]&&a.splice(a.length-1,1);var b=[],d="",c;for(c=0;6>c;c++)d+=a[c]+"/";b.push({name:'\x3ci class\x3d"fa fa-home"\x3e\x3c/i\x3e',tooltip:"/",url:d});for(c=6;c<a.length;c++)d+=a[c],b.push({name:a[c],tooltip:a[c],url:d}),d+="/";return b},selectedItemCount:function(){var a=
0;this.elements.forEach(function(b){b.selected&&a++});return a},selectedFileCount:function(){var a=0,b=this;this.elements.forEach(function(d){d.selected&&b.isFile(d)&&a++});return a},hasDirectory:function(){for(var a=0;a<this.elements.length;a++)if(this.isDirectory(this.elements[a]))return!0;return!1},hasFile:function(){for(var a=0;a<this.elements.length;a++)if(this.isFile(this.elements[a]))return!0;return!1},colSizeClasses:function(){return["col-"+this.size.suffix[this.size.countPerRow-1],"col-sm-"+
this.size.suffix[this.size.countPerRow],"col-md-"+this.size.suffix[this.size.countPerRow+1],"col-xl-"+this.size.suffix[this.size.countPerRow+2]]},heightSizeClasses:function(){return["height-vw-"+this.size.suffix[this.size.countPerRow-1],"height-vw-sm-"+this.size.suffix[this.size.countPerRow],"height-vw-md-"+this.size.suffix[this.size.countPerRow+1],"height-vw-xl-"+this.size.suffix[this.size.countPerRow+2]]},images:function(){var a=[];this.sortedFiles.forEach(function(b){a.push(b.data)});return a},
faArrowSortIcon:function(){var a=[];a.push("fa");this.sortOptions.isAscend()?a.push("fa-arrow-up"):a.push("fa-arrow-down");return a},isSelectAll:function(){return 0<this.elements.length&&this.selectedItemCount===this.elements.length},faSelectedIcon:function(){return this.isSelectAll?["far","fa-check-circle"]:["far","fa-circle"]}},methods:{dateConvert:function(a){a=eval(a);return(new Date(a)).toLocaleString()},sortBy:function(a){this.sortOptions.changeOrder(a)},isSortedByField:function(a){return this.sortOptions.fieldName===
a},filter:function(a){var b=this,d=[];this.elements.forEach(function(c){("DIR"===a&&b.isDirectory(c)||"FILE"===a&&b.isFile(c))&&d.push(c)});return d},pageNotFound:function(){var a=$('\x3cform action\x3d"/page-not-found" method\x3d"post" hidden\x3d"hidden"\x3e\x3cinput type\x3d"text" name\x3d"url" value\x3d"'+window.location.href+'" /\x3e\x3c/form\x3e');$("body").append(a);a.submit()},loadDirectoryData:function(){var a=this;a.showLoader=!0;axios.get(apiUrl("directory")).then(function(b){200===b.status?
(b.data.forEach(function(b){a.addElement(b)}),a.allUsers=b.data):a.pageNotFound();a.showLoader=!1})["catch"](function(b){a.showLoader=!1;a.pageNotFound()})},addElement:function(a){a.uri=a.parentUri+"/"+a.name;this.elements.push({data:a,selected:!1})},isDirectory:function(a){return"DIR"===a.data.type},isFile:function(a){return"FILE"===a.data.type},clearErrorCode:function(){this.errorCode=""},makeError:function(a,b){console.log("code "+b+": "+a);this.errorCode=b},onFileUpload:function(){console.log("start files upload");
this.$refs.files.click()},onFileUploadFinish:function(){console.log("Files upload finish\n");for(var a=this.$refs.files.files,b=new FormData,d=0;d<a.length;d++)b.append("files",a[d]);var c=this;axios.post(apiUrl("files"),b,{headers:{"Content-Type":"multipart/form-data"}}).then(function(a){200===a.status?a.data.forEach(function(a){c.addElement(a)}):console.log("Error in uploading files (status \x3d "+a.status+")")})["catch"](function(a){console.log("Error in uploading files (status \x3d "+a.response.status+
")")})},onCreateDirectory:function(){this.editedValue="";this.clearErrorCode();this.dialogOperationInProgress=!1;$("#createDirectory .singleInputModalForm").modal("show")},onCreateDirectorySuccess:function(a){this.clearErrorCode();console.log(a);var b=this,d=apiUrl("directory");b.dialogOperationInProgress=!0;axios.post(d,a,{headers:{"Content-Type":"text/plain"}}).then(function(c){201===c.status?(b.errorCode="",$("#createDirectory .singleInputModalForm").modal("hide"),b.addElement(c.data)):b.makeError("Response on create directory \x3d "+
a,c.status);b.dialogOperationInProgress=!1})["catch"](function(c){b.makeError("Response on create directory \x3d "+a,c.response.status);b.dialogOperationInProgress=!1})},onRename:function(){if(1!==this.selectedItemCount)console.log("Renaming failure");else{for(var a=0;a<this.elements.length;a++)if(this.elements[a].selected){this.editedItem=this.elements[a];this.editedValue=this.elements[a].data.name;break}console.log("rename:   ");console.log(this.editedValue);$("#renameItem .singleInputModalForm").modal("show")}},
onRenameSuccess:function(){var a=this;a.clearErrorCode();a.dialogOperationInProgress=!0;axios.put(this.editedItem.data.uri,{parentUri:this.editedItem.data.parentUri,name:this.editedValue,type:this.editedItem.data.type}).then(function(b){200===b.status?(a.errorCode="",a.editedItem.data.name=a.editedValue,a.editedItem.selected=!1,$("#renameItem .singleInputModalForm").modal("hide")):a.makeError("Response on rename \x3d "+a.editedValue,b.status);a.dialogOperationInProgress=!1})["catch"](function(b){a.makeError("Response on rename \x3d "+
a.editedValue,b.response.status);a.dialogOperationInProgress=!1})},onDelete:function(){1>this.selectedItemCount?console.log("No selected elements"):(console.log("delete:   "),console.log(this.selectedItemCount),$("#deleteItems #confirmModalForm").modal("show"))},onDeleteConfirm:function(){var a=this;this.errorCode="-1";var b=this.selectedItemCount;this.elements.forEach(function(d,c,f){d.selected&&axios["delete"](d.data.uri).then(function(d){200===d.status?f.splice(c,1):a.makeError("Response on rename \x3d "+
a.editedValue,d.status);b--;0===b&&$("#deleteItems #confirmModalForm").modal("hide")})["catch"](function(c){a.makeError("Response on rename \x3d "+a.editedValue,c.response.status);b--;0===b&&$("#deleteItems #confirmModalForm").modal("hide")})})},onMove:function(){1>this.selectedItemCount?console.log("Moving failure"):(console.log("moving:   "),console.log(this.selectedItemCount),this.editedValue=apiUrl("directory"),this.$refs.moveModalForm.loadSubDirectories(apiUrl("directories")),$("#moveItems .moveModalForm").modal("show"))},
onMoveConfirm:function(a){var b=a.replace("/api/","/user/").replace("/directories/","/drive/"),d=this;d.operationInProgress=!0;var c=this.selectedItemCount;this.elements.forEach(function(a,e,g){a.selected&&(b===a.data.parentUri?(c--,0===c&&$("#moveItems .moveModalForm").modal("hide")):axios.put(a.data.uri,{parentUri:b,name:a.data.name,type:a.data.type}).then(function(b){200===b.status?g.splice(e,1):d.makeError("Response on moving \x3d "+a.data.uri,b.status);c--;0===c&&$("#moveItems .moveModalForm").modal("hide")})["catch"](function(b){d.makeError("Response on moving \x3d "+
a.data.uri,b.response.status);c--;0===c&&$("#moveItems .moveModalForm").modal("hide")}))})},onTweet:function(){this.editedValue="";this.clearErrorCode();this.dialogOperationInProgress=!1;$("#createTweetMessage .singleTextareaModalForm").modal("show")},onTweetSuccess:function(){var a=this;console.log("Create tweet...\n");var b=[];this.elements.forEach(function(c,d,e){c.selected&&!a.isDirectory(c)&&b.push(c.data.uri)});var d={paths:b,message:a.editedValue};a.dialogOperationInProgress=!0;axios.post(shortApiUrl("tweet"),
JSON.stringify(d),{headers:{"Content-Type":"application/json"}}).then(function(b){200===b.status?($("#createTweetMessage .singleTextareaModalForm").modal("hide"),a.showTweetPublishedForm(b.data)):(a.errorCode=b.status,a.error=b.data);a.dialogOperationInProgress=!1})["catch"](function(b){a.errorCode=b.response.status;a.error=b.response.data;a.dialogOperationInProgress=!1})},selectAll:function(a){this.elements.forEach(function(b){b.selected=a})},changeAllSelection:function(){this.selectAll(!this.isSelectAll)},
selectDirectories:function(){this.filter("DIR").forEach(function(a){a.selected=!0})},selectFiles:function(){this.filter("FILE").forEach(function(a){a.selected=!0})},showTweetPublishedForm:function(a){this.errorCode="";this.editedValue=a.url;this.selectAll(!1);$("#tweetPublished .singleInputModalForm").modal("show")},hideTweetPublishedForm:function(){$("#tweetPublished .singleInputModalForm").modal("hide")},sizeMenuItemClass:function(a){return a!==this.size.countPerRow?"hidden":""},sortOption:function(a){return this.isSortedByField(a)?
"":"hidden"},sortingDirection:function(){this.sortOptions.changeOrder(this.sortOptions.fieldName)}},created:function(){this.url=decodeURI(document.URL);this.loadDirectoryData();$("#tweetPublished .singleInputModalForm .btn-secondary").hide()}});