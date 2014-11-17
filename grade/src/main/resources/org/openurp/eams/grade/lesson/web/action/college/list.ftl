[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>

[@b.form name="gradeListForm" action="!search" target="contentDiv"]
	<input name="params" value="${b.paramstring}" type="hidden"/>
	[@b.grid items=lessons var="lesson" filterable="true"]
		[@b.gridbar]
		    bar.addItem("查看", action.single("info"));
			bar.addItem("空白登分表", function () {
				var lessonIds = bg.input.getCheckBoxValues("lesson.id");
				if (lessonIds == null || lessonIds == "") {
		    		alert("请选择一个或多个教学任务进行操作!");
		    		return false;
		    	}
	     		bg.form.submit(document.gradeListForm, "${b.url('report!blank')}", "_blank");
	    	}, "print.png");
	    	
		    bar.addItem("录入", function () {
		       	var lessonIds = bg.input.getCheckBoxValues("lesson.id");
		       	if (lessonIds == null || lessonIds == "") {
		          	alert("请仅选择一个教学任务.");
		          	return false;
		       	}
	       		bg.form.submit(document.gradeListForm,"${b.url('!inputReady')}", "_blank");
		 	}, "new.png");
			
			var reportMenu = bar.addMenu("统计", null, "print.png");
			reportMenu.addItem("任务分段统计", "printStatReport('task')");
			reportMenu.addItem("课程分段统计", "printStatReport('course')");
			var printStatReport = function(kind) {
				var lessonIds = bg.input.getCheckBoxValues("lesson.id");
				if (lessonIds == null || lessonIds == "") {
		    		alert("请选择一个或多个教学任务进行操作!");
		    		return false;
		    	}
			    for(var i=0;i<seg.length;i++){
		          var segAttr="segStat.scoreSegments["+i+"]";
		          bg.form.addInput(document.gradeListForm, segAttr+".min",seg[i].min);
		          bg.form.addInput(document.gradeListForm, segAttr+".max",seg[i].max);
		        }
		        if(null==kind){
		           kind="task";
		        }
		        bg.form.addInput(document.gradeListForm,"kind",kind);
		        bg.form.addInput(document.gradeListForm,"scoreSegmentsLength",seg.length);
	   			bg.form.submit(document.gradeListForm, "${b.url('!stat')}", "_blank");
			};
			
		    var printMenu = bar.addMenu("打印", null, "print.png");
		    [#list gradeTypes as gradeType]
			printMenu.addItem("${gradeType.name}", "printTeachClassGrade('${(gradeType.id)!}')");
			[/#list]
			var printTeachClassGrade = function(gradeTypeIds){
				var form = document.gradeListForm;
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
		        if (null == lessonIds || "" == lessonIds) {
		          	alert("请选择一个或多个教学任务进行操作!");
	            	return;
		        }
		        bg.form.submit(form,"${b.url('!report')}","_blank");
			}
			
			var publishMenu = bar.addMenu("发布");
			[#list publishableGradeTypes as gradeType]
			publishMenu.addItem("${gradeType.name}", "publishCancelGrade(${gradeType.id}, true)");
			[/#list]
			var publishCancelGrade = function(gradeTypeId, isPublished) {
				var form = document.gradeListForm;
				var lessonIds = bg.input.getCheckBoxValues("lesson.id");
		        if (null == lessonIds || "" == lessonIds) {
		            alert("请选择一个或多个教学任务进行操作!");
		            return;
		        }
		        if (confirm(isPublished ? "确定要发布" + (null == gradeTypeId ? "所有" : "当前指定的") + "成绩吗？" : "确定要取消发布吗？")) {
		            bg.form.addInput(form, "isPublished", isPublished, "hidden");
		            if (null != gradeTypeId) {
		                bg.form.addInput(form, "gradeTypeId", gradeTypeId, "hidden");
		            }
		            if(isPublished) {
		            	bg.form.submit(form,"${b.url('!publish')}");
		            }else{
		            	bg.form.submit(form,"${b.url('!revoke')}");
		            }
		        }
			}
		[/@]
	  	[@b.gridfilter property="teachers[0].name"]
	  		<input name="teacher.name" type="text" style="width:95%;" value="${(Parameters['teacher.name'])!}" maxlength="100" />
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="no" title="attr.taskNo" width="10%"/]
			[@b.col property="course.code" title="attr.courseNo" width="10%"/]
			[@b.col property="course.name" title="attr.courseName" width="22%"]
			${(lesson.course.name)?if_exists}
			 [#if lesson.subCourse??]
			   <sup style="color:#29429E;">${lesson.subCourse.name}</sup>
			 [/#if]
			[/@]
			[@b.col property="teachClass.name" title="entity.teachClass" width="35%"/]
			[@b.col width="10%" property="teachers[0].name" title="entity.teacher" sortable="false"][@getTeacherNames lesson.teachers/][/@]
			[@b.col  title="attr.stdNum" width="5%" property="teachClass.stdCount"]
				[@b.a href='/teachTaskCollege!printAttendanceCheckList?lessonIds=${lesson.id}' title='查看点名册' target='_blank']${lesson.teachClass.stdCount}[/@]
			[/@]
			[@b.col property="course.credits" title="attr.credit" width="5%"/]
			[@b.col property="coursePeriod" title="课时" width="5%"/]
		[/@]
	[/@]
[/@]

[@b.foot/]
