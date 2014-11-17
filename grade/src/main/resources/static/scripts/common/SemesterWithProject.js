    // 缺省值
    var defaultSemesterValues=new Object();
    // 页面上所有的三级级联选择
    var projectSelects= new Array();
    // 当前的三级级联选择
    var yearSelectQueue=new Array();
    var termSelectQueue=new Array();
    var selectInited=new Object();
    // 初始化校历选择框
    function initProjectSelect1(projects){
        if(null==selectInited[this.projectSelectId]){
           selectInited[this.projectSelectId]=true;
        }else{
           return;
        }
        if( null==document.getElementById(this.projectSelectId)) return;
        defaultSemesterValues[this.projectSelectId]=document.getElementById(this.projectSelectId).value;
        dwr.util.removeAllOptions(this.projectSelectId);
        if(this.projectDefaultFirst){
          	dwr.util.addOptions(this.projectSelectId,projects,'id','name');
          	if(this.projectNullable){
               dwr.util.addOptions(this.projectSelectId,[{'id':'','name':'...'}],'id','name');
            }
        }else{
          	if(this.projectNullable){
               dwr.util.addOptions(this.projectSelectId,[{'id':'','name':'...'}],'id','name');
            }
            dwr.util.addOptions(this.projectSelectId,projects,'id','name');
        }
        
        // 让非空的学生类别作为默认
        if(""!=defaultSemesterValues[this.projectSelectId])
	        setSelected(document.getElementById(this.projectSelectId),defaultSemesterValues[this.projectSelectId]);
       
        var selfOnchange =document.getElementById(this.projectSelectId).onchange;
        document.getElementById(this.projectSelectId).onchange=function (event){
            if(event==null)
               event=getEvent();
	        notifyYearChange(event);
	        if(selfOnchange!=null)
    	        selfOnchange();
	    }
    }
    // 初始化学年度选择框
    function initYearSelect(){
       dwr.util.removeAllOptions(this.yearId);
       if (this.yearNullable) {
           dwr.util.addOptions(this.yearId,[{'id':'','name':'...'}],'id','name');
       }
       var std= document.getElementById(this.projectSelectId);
       if(std.value!=""){
            yearSelectQueue.push(this);
       		semesterDao.getYearsOrderByDistanceWithProject(std.value,setYearOptions);
       }
       document.getElementById(this.yearId).onchange = function (event) {
           notifyTermChange(event);
       }
    }

    
    // 通知学年度变化,填充学年度选择列表
    function notifyYearChange(event){
       if(event==null)return;
       //alert("event in notifyYearChange"+event);
       yearProjectSelects = getMyProjectSelects(getEventTarget(event).id);
       //alert(yearProjectSelects.length);
       for(var i=0;i<yearProjectSelects.length;i++){
	       var s= document.getElementById(yearProjectSelects[i].projectSelectId);
	       if(null==s) continue;
	       dwr.util.removeAllOptions(yearProjectSelects[i].yearId);
	       if(s.value!=""){
	           yearSelectQueue.push(yearProjectSelects[i]);
		       //semesterDao.getYearsOrderByDistance(s.value,setYearOptions);
	           semesterDao.getYearsOrderByDistanceWithProject(s.value,setYearOptions);
	       }else{
	          dwr.util.removeAllOptions(yearProjectSelects[i].termId);
	       }
       }
    }
    function setYearOptions(data){
       var curProjectSelect=yearSelectQueue.shift();
       if(null!=curProjectSelect){
	       if(curProjectSelect.yearNullable){
	           dwr.util.addOptions(curProjectSelect.yearId,[{'id':'','name':'...'}],'id','name');
	       }
	       dwr.util.addOptions(curProjectSelect.yearId,data);
	       if(defaultSemesterValues[curProjectSelect.yearId]!=""){
	           setSelected(document.getElementById(curProjectSelect.yearId),defaultSemesterValues[curProjectSelect.yearId]);
	       }
	       notifyTermChange(null,curProjectSelect.yearId);
       }
    }
    // 通知学期变化，填充学期列表
    function notifyTermChange(event,selectId){
       //alert("event in notifyTermChange");
       if(null==selectId) {
         selectId=getEventTarget(event).id;
       }
       myProjectSelects= getMyProjectSelects(selectId);
       for(var i=0;i<myProjectSelects.length;i++){
           //alert("removeAllOptions of :"+myProjectSelects[i].termId);
	       dwr.util.removeAllOptions(myProjectSelects[i].termId);
	       var s= document.getElementById(myProjectSelects[i].projectSelectId);
	       var y= document.getElementById(myProjectSelects[i].yearId);
	       
	       if(s.value!=""&&y.value!=null){
  	          termSelectQueue.push(myProjectSelects[i]);
	          //alert(curProjectSelect.termId)
	          semesterDao.getTermsOrderByDistanceWithProject(s.value,y.value,setTermOptions);
	       }else{
	          dwr.util.removeAllOptions(myProjectSelects[i].termId);
	       }
       }
    }
    function setTermOptions(data){
       //alert(data)
       var curProjectSelect=termSelectQueue.shift();
       if(null!=curProjectSelect){
           dwr.util.removeAllOptions(curProjectSelect.termId);
	       dwr.util.addOptions(curProjectSelect.termId,data); 
	            
           if(curProjectSelect.termNullable){
              //alert("add null term");
              dwr.util.addOptions(curProjectSelect.termId,[{'id':'','name':'...'}],'id','name');
           }
	       if(defaultSemesterValues[curProjectSelect.termId]!=""){
	           setSelected(document.getElementById(curProjectSelect.termId),defaultSemesterValues[curProjectSelect.termId]);
	       }
       }
    }
    function SemesterSelect(projectSelectId,yearId,termId,projectNullable,yearNullable,termNullable,projectDefaultFirst){
      this.projectSelectId=projectSelectId;
      this.yearId=yearId;
      this.termId=termId;
      this.projectDefaultFirst=projectDefaultFirst=projectDefaultFirst==null;
      this.initProjectSelect1=initProjectSelect1;
      this.initYearSelect=initYearSelect;
      this.projectNullable=projectNullable;
      this.yearNullable=yearNullable;
      this.termNullable=termNullable;
      this.init=initProject;
      this.getdefaultSemesterValues=getdefaultSemesterValues;
      projectSelects[projectSelects.length]=this;
      this.getdefaultSemesterValues();
    }
    
    function initProject(projects){
       this.initProjectSelect1(projects);
       this.initYearSelect();
    }
    function getMyProjectSelects(id){
        var myProjectSelects = new Array();
        for(var i=0;i<projectSelects.length;i++){
            if(projectSelects[i].projectSelectId==id||
               projectSelects[i].yearId==id||
               projectSelects[i].termId==id)
               myProjectSelects[myProjectSelects.length]=projectSelects[i];
        }
        return myProjectSelects;
    }
    /**
     * 获得缺剩值
     */
    function getdefaultSemesterValues(){
       defaultSemesterValues[this.projectSelectId]=document.getElementById(this.projectSelectId).value;
       defaultSemesterValues[this.yearId]=document.getElementById(this.yearId).value;
       defaultSemesterValues[this.termId]=document.getElementById(this.termId).value;
       //alert(defaultSemesterValues[this.termId]);
    }