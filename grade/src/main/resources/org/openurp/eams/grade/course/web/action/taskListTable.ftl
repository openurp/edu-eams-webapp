[#ftl]
[#include "/template/macros.ftl"/]
[@b.form name="gradeListForm" action="!search" target="contentDiv"]
<input name="params" value="${b.paramstring}" type="hidden"/>
[@b.grid items=lessons var="lesson" filterable="true"]
	[@b.gridbar]
	    bar.addItem("查看", "info(document.gradeListForm)");
		[#if (Parameters["status"]!"0") != "0"]
		bar.addItem("${gradeType.name}打印", "printTeachClassGrade(document.gradeListForm, '${gradeType.id}')");
		//printMenu.addItem("任务分段统计", "printStatReport(document.gradeListForm, 'task')");
		//printMenu.addItem("成绩分段统计", "printStatReport(document.gradeListForm, 'course')");
		//printMenu.addItem("试卷分析", "printExamReport(document.gradeListForm)");
		bar.addItem("发布${gradeType.name}", "publishCancelGrade(document.gradeListForm, ${gradeType.id}, true)");
		[#else]
		bar.addItem("录入/百分比", "inputTask()", "new.png", "录入成绩/调整百分比，只能是单个任务的操作");
		[/#if]
		[#if Parameters["status"]?default("0") != "2"]
		[@ems.guard res="/teach/grade/course/manage"]
		bar.addItem("删除${gradeType.name}", "removeGrade(${gradeType.id},'${gradeType.name}', '确定要删除该教学任务下的所有${gradeType.name}成绩吗？')");
		[/@]
		[/#if]
	[/@]
  	[@b.gridfilter property="teachers[0].name"]
  		<input name="teacher.name" type="text" style="width:95%;" value="${(Parameters['teacher.name'])!}" maxlength="100" />
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="no" title="attr.taskNo" width="10%"/]
		[@b.col property="course.code" title="attr.courseNo" width="10%"/]
		[@b.col property="course.name" title="attr.courseName" width="22%"/]
		[@b.col property="teachClass.name" title="entity.teachClass" width="35%"/]
		[@b.col width="10%" property="teachers[0].name" title="entity.teacher" sortable="false"][@getTeacherNames lesson.teachers/][/@]
		[@b.col  title="attr.stdNum" width="5%" property="teachClass.stdCount"]
			[@b.a href='/teachTask!printAttendanceCheckList?lessonIds=${lesson.id}' title='查看点名册' target='_blank']${lesson.teachClass.stdCount}[/@]
		[/@]
		[@b.col property="course.credits" title="attr.credit" width="5%"/]
		[@b.col property="course.period" title="课时" width="5%"/]
	[/@]
[/@]
<script language="JavaScript">
    jQuery(function(){
		bg.form.addHiddens(document.gradeListForm, "[@htm.queryStr/]");
	});
</script>
[/@]