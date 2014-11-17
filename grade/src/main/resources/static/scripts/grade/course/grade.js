var action = "";
	 //打印分段统计
	 function printStatReport(form,kind){
	    form.target="_blank";
	    form.action=action+"?method=stat";
	    for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          addInput(form,segAttr+".min",seg[i].min);
          addInput(form,segAttr+".max",seg[i].max);
        }
        if(null==kind){
           kind="task";
        }
        addInput(form,"kind",kind);
        addInput(form,"scoreSegmentsLength",seg.length);
	    submitId(form,"lessonId",true);
	    form.target="_self";
	 }
     //打印试卷分析
     function printExamReport(form){
        form.target="_blank";
        form.action="teacherGrade.action?method=reportForExam";
	    for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          addInput(form,segAttr+".min",seg[i].min);
          addInput(form,segAttr+".max",seg[i].max);
        }
        addInput(form,"scoreSegmentsLength",seg.length);
        submitId(form,"lessonId",true);
        form.target="_self";
     }
	 //打印教学班成绩
	 function printTeachClassGrade(form, gradeTypeIds){
	    var formAction = "";
	    if (null != action) {
	    	formAction = action;
	    }
        form.target="_blank";
        if (null != gradeTypeIds && "" != gradeTypeIds) {
           if (null == form["gradeTypeIds"]) {
             addInput(form, "gradeTypeIds", gradeTypeIds, "hidden");
           } else {
             form["gradeTypeIds"].value = gradeTypeIds;
           }
        } else {
           if (null != form["gradeTypeIds"]) {
             form["gradeTypeIds"].value = "";
           }
        }
        submitId(form,"lessonId",true,action+"?method=report");
        form.target="_self";
	 }
	 //查看成绩信息
     function info(form){
        submitId(form,"lessonId",false,action+"?method=info&orderBy=std.code asc");
     }
     //成绩录入
     function inputGrade(){
       var lessonId = getSelectIds("lessonId");
       if(""==lessonId || isMultiId(lessonId)){
          alert("请仅选择一个教学任务.");
          return;
       }
       window.open("teacherGrade.action?method=input&lessonId="+lessonId);
     }
     
     //录入成绩欢迎界面
     function inputTask(){
       var lessonId = getSelectIds("lessonId");
       if(""==lessonId || isMultiId(lessonId)){
          alert("请仅选择一个教学任务.");
          return;
       }
       window.open("teacherGrade.action?method=inputTask&lessonId="+lessonId);
     }
     //删除考试成绩
     function removeGrade(gradeTypeId){
       var form =document.actionForm;
       setSearchParams();
       submitId(form,"lessonId",false,action+"?method=removeGrade&gradeTypeId="+gradeTypeId,"确认删除成绩?");
     }
     
     function editGradeState(form){
        submitId(form,"lessonId",false,action+"?method=editGradeState");
     }
     
     //暂存查询参数
	 function setSearchParams(){
	    var params = getInputParams(parent.document.taskForm,null,false);
	    document.actionForm.params.value=params;
	 }
	 function editGradeStateInfo(form){
	    submitId(form,"lessonId",false,"courseGrade.action?method=editGradeState");
	 }
    function publishCancelGrade(form, gradeTypeId, isPublished) {
        var lessonIds = getSelectIds("lessonId");
        if (null == lessonIds || "" == lessonIds) {
            alert("请选择要操作的记录。");
            return;
        }
        if (confirm(isPublished ? "确定要发布" + (null == gradeTypeId ? "所有" : "当前指定的") + "成绩吗？" : "确定要取消发布吗？")) {
            form.action = "courseGrade.action?method=publishCancelGrade";
            addInput(form, "isPublished", isPublished, "hidden");
            if (null != gradeTypeId) {
                addInput(form, "gradeTypeId", gradeTypeId, "hidden");
            }
            addInput(form, "lessonIds", lessonIds, "hidden");
            setSearchParams();
            form.submit();
        }
    }