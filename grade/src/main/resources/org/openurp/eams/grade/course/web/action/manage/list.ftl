[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>
    [#include "../taskListTable.ftl"/]
    <script>
    //打印教学班成绩
	function printTeachClassGrade(form, gradeTypeIds){
        form.target="_blank";
        if (null != gradeTypeIds && "" != gradeTypeIds) {
           if (null == form["gradeTypeIds"]) {
             bg.form.addInput(form, "gradeTypeIds", gradeTypeIds, "hidden");
           } else {
             form["gradeTypeIds"].value = gradeTypeIds;
           }
        } else {
           if (null != form["gradeTypeIds"]) {
             form["gradeTypeIds"].value = "";
           }
        }
        var lessonIds = bg.input.getCheckBoxValues("lesson.id");
        if("" == lessonIds){
        	alert("请选择一个或多个进行操作!");
        	form.target="contentDiv";
        	return false;
        }
        bg.form.addInput(form,"lessonIds",lessonIds);
        bg.form.submit(form,"${b.url('manage!report')}");
        form.action="${b.url('manage!search')}";
        form.target="contentDiv";
	}
	
	function publishCancelGrade(form, gradeTypeId, isPublished) {
		var lessonIds = bg.input.getCheckBoxValues("lesson.id");
        if (null == lessonIds || "" == lessonIds) {
            alert("请选择要操作的记录。");
            return;
        }
        if (confirm(isPublished ? "确定要发布" + (null == gradeTypeId ? "所有" : "当前指定的") + "成绩吗？" : "确定要取消发布吗？")) {
            bg.form.addInput(form, "isPublished", isPublished);
            if (null != gradeTypeId) {
                bg.form.addInput(form, "gradeTypeId", gradeTypeId, "hidden");
            }
            bg.form.addInput(form, "lessonIds", lessonIds);
            bg.form.addInput(form,"status","${status!}");
            bg.form.submit(form,"${b.url('manage!publishCancelGrade')}");
            form.action="${b.url('manage!search')}";
        }
	}
	
	//录入成绩欢迎界面
	function inputTask(){
       var lessonId = bg.input.getCheckBoxValues("lesson.id");
       if(isEmpty(lessonId) || lessonId.indexOf(",")>0){
          alert("请仅选择一个教学任务.");
          return;
       }
       window.open("${b.url('admin!inputTask')}?lessonId="+lessonId);
 	}
  	
  	//删除考试成绩
	function removeGrade(gradeTypeId, gradeTypeName, additionalMsg){
		var form = document.gradeIndexForm;
		var lessonId = bg.input.getCheckBoxValues("lesson.id");
		if(isEmpty(lessonId) || lessonId.indexOf(",")>0){
			alert("请仅选择一个教学任务.");
        	return;
		}
       	bg.form.addInput(form,"gradeTypeId",gradeTypeId);
       	bg.form.addInput(form,"lessonId",lessonId);
       	if(!confirm(autoLineFeed(isEmpty(additionalMsg) ?"删除" + gradeTypeName + "的同时会将其状态置为“未录入”，\n要继续吗？" : additionalMsg)))return;
       	bg.form.submit(form,"${b.url('manage!removeGrade')}");
 		form.action="${b.url('manage!search')}";
	}
	
	function editGradeState(form){
		var lessonId = bg.input.getCheckBoxValues("lesson.id");
		if(isEmpty(lessonId) || lessonId.indexOf(",")>0){
			alert("请仅选择一个教学任务.");
        	return;
		}
       	bg.form.addInput(form,"lessonId",lessonId);
       	bg.form.addInput(form,"status","${status}");
   		bg.form.submit(form,"${b.url('manage!editGradeState')}");
 		form.action="${b.url('manage!search')}";
 	}
 	
 	//查看成绩信息
 	function info(form){
 		var lessonId = bg.input.getCheckBoxValues("lesson.id");
		if(isEmpty(lessonId) || lessonId.indexOf(",")>0){
			alert("请仅选择一个教学任务.");
        	return;
		}
       	bg.form.addInput(form,"lessonId",lessonId);
   		bg.form.submit(form,"${b.url('manage!info')}?orderBy=std.code asc");
 		form.action="${b.url('manage!search')}";
 	}
 	
 	 //打印分段统计
	 function printStatReport(form,kind){
	    form.target="_blank";
	    form.action="${b.url('manage!stat')}";
	    for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          bg.form.addInput(form,segAttr+".min",seg[i].min);
          bg.form.addInput(form,segAttr+".max",seg[i].max);
        }
        if(null==kind){
           kind="task";
        }
        bg.form.addInput(form,"kind",kind);
        bg.form.addInput(form,"scoreSegmentsLength",seg.length);
	    bg.form.submitId(form,"lesson.id",true);
	   	form.target="contentDiv";
	   	form.action="${b.url('manage!search')}";
	 }
     //打印试卷分析
     function printExamReport(form){
        form.target="_blank";
        form.action="${b.url('admin!reportForExam')}";
	    for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          bg.form.addInput(form,segAttr+".min",seg[i].min);
          bg.form.addInput(form,segAttr+".max",seg[i].max);
        }
        bg.form.addInput(form,"scoreSegmentsLength",seg.length);
        bg.form.submitId(form,"lesson.id",true);
        form.target="contentDiv";
        form.action="${b.url('manage!search')}";
     }
     
     //成绩登记表打印
     function printGradeRegistrationForm(form){
        form.target="_blank";
        bg.form.submitId(form,"lesson.id",true,"${b.url('report!report')}");
        form.target="contentDiv";
        form.action="!search";
     }
    </script>
[@b.foot/]
