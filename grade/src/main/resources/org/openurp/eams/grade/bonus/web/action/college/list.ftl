[#ftl]
[#include "/template/macros.ftl"/]
[@b.form name="bonusListForm" action="!search" target="contentDiv"]
[@b.grid items=examGrades var="examGrade"]
	[@b.gridbar]
		[#if inputOpen??]
			bar.addItem("加分管理",action.add());
		[/#if]
		bar.addItem("打印加分成绩","report()");
		function report(){
			var form = document.bonusListForm;
			var examGradeIds = bg.input.getCheckBoxValues("examGrade.id");
			if(examGradeIds != null && examGradeIds != ""){
				bg.form.addInput(form,"examGradeIds",examGradeIds);
			}else{
				bg.form.addInput(form,"examGradeIds","");
			}
			bg.form.submit(form,"${b.url('!report')}","_blank");
		}
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="courseGrade.std.code" title="attr.stdNo" width="10%"/]
		[@b.col property="courseGrade.std.name" title="attr.personName" width="20%"/]
		[@b.col property="courseGrade.lesson.no" title="attr.taskNo" width="15%"/]
		[@b.col property="courseGrade.lesson.course.code" title="attr.courseNo" width="15%"/]
		[@b.col property="courseGrade.lesson.course.name" title="attr.courseName" width="20%"/]
		[@b.col property="courseGrade.lesson.courseType.name" title="common.courseType" width="10%"/]
		[@b.col property="scoreText" title="加分成绩" width="10%"/]
	[/@]
[/@]
[/@]
<script language="JavaScript">
    jQuery(function(){
		bg.form.addHiddens(document.bonusListForm, "[@htm.queryStr/]");
	});
</script>