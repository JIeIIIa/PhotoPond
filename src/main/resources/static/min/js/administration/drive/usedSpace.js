var usedSpace=Vue.component("used-space",{data:function(){return{statisticsDTO:[],showLoader:!1,filterTemplate:"",errorCode:"",message:""}},computed:{filteredData:function(){if(""===this.filterTemplate)return this.statisticsDTO;var a=this.filterTemplate.toLocaleLowerCase();return this.statisticsDTO.filter(function(b){return-1<String(b.login).toLocaleLowerCase().indexOf(a)})}},methods:{loadInformation:function(){var a=this;a.showLoader=!0;axios.get(adminUrlTemplate.drive.statistics).then(function(b){a.statisticsDTO=
b.data;a.showLoader=!1})["catch"](function(b){a.showLoader=!1})}},created:function(){this.loadInformation()}});