[#ftl]
[#include "/template/macros.ftl"/]
[@b.head/]
[@b.form action="!save" target="contentDiv" name="bonusSaveForm"]
	<input type="hidden" value="${(student.id)!}" name="student.id"/>
	[@b.grid items=courseTakes?sort_by(["lesson","no"]) var="courseTake"]
		[@b.gridbar]
			[#if student??]
			bar.addItem("${b.text('action.save')}","bg.form.submit(document.bonusSaveForm)");
			[/#if]
		[/@]
		<tr>
			<td colSpan="4">统一设置</th>
			<td><input type="text" value="" id="allBonus" onBlur="changeAll(this)"/></td>
		</tr>
		[@b.row]
			[@b.col property="lesson.no" title="attr.taskNo"/]
			[@b.col property="lesson.course.code" title="attr.courseNo" /]
			[@b.col property="lesson.course.name" title="attr.courseName"/]
			[@b.col title="最终成绩"]${(lessonGrades.get(courseTake.lesson).scoreText)!}[#if (lessonGrades.get(courseTake.lesson).score)??](${lessonGrades.get(courseTake.lesson).score})[/#if][/@]
			[@b.col title="加分成绩"]<input type="hidden" value="${courseTake.lesson.id}" name="lesson.id"/><input type="text" value="${(lessonGrades.get(courseTake.lesson).getExamGrade(bonus).scoreText)!}" name="bonus_score_${courseTake.lesson.id}" onBlur="validate(this)" role="bonusInput"/>[/@]
		[/@]
	[/@]
[/@]
<script language="JavaScript">
	jQuery(document).ready(function(){
		[#if student??]
			jQuery("#personName").text("[@i18nName student/]");
			jQuery("#major").text("[@i18nName student.major/]");
			jQuery("#department").text("[@i18nName student.department/]");
			jQuery("#grade").text("${(student.grade)!}");
			jQuery("#stdType").text("[@i18nName student.type/]");
		[#else]
			jQuery("#personName").text("查无此人");
			jQuery("#major").text("");
			jQuery("#department").text("");
			jQuery("#grade").text("");
			jQuery("#stdType").text("");
		[/#if]
	})
	
	function changeAll(obj){
		if(validate(obj)){
			jQuery("input[role='bonusInput']").val(obj.value);
		}
	}
	function validate(obj){
		if(isNaN(obj.value)){
			obj.value="";
			return false;
		}else if(obj.value < 0){
			obj.value = "0";
		}else if(obj.value > 100){
			obj.value = "100";
		}
		return true;
	}
</script>
[@b.foot/]