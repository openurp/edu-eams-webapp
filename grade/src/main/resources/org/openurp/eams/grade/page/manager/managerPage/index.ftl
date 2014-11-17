[#ftl]
[@b.head/]
[@b.toolbar title="课程成绩"]
	bar.addItem("不及格成绩", "noPassCourseGrades()");
	bar.addItem("无成绩学生名单", "noGradeTakes()");
		
[/@]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
<table class="indexpanel">
	<tr>
		<td class="index_view" style="width:180px">
		[@b.form name="gradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
			[#assign extraSearchTR]
				[@b.select label="最终成绩" name="fake.final.status" items={"0":"未提交","1":"已提交","2":"已发布"} empty="..." /]
				[@b.field]
					<fieldset>
						<legend style="text-align:left">考试成绩:</legend>
						<label>成绩类型:</label>[@b.select name="fake.examGrade.gradeType.id" items=gradeTypes theme="xml" empty="..." style="width:90px"/]<br>
				    	<label>录入状态:</label>[@b.select name="fake.examGrade.status" items={"0":"未提交","1":"已提交","2":"已发布"} theme="xml" empty="..." style="width:90px"/]
				    </fieldset>
				[/@]
			[/#assign]
			[#include "../common/taskBasicForm.ftl"/]
		[/@]
	   	</td>
		<td class="index_content">
			[@b.div id="contentDiv" /]
		</td>
	</tr>
</table>
<script>
jQuery(function() {
	bg.form.submit(document.gradeIndexForm);
});
</script>
[@b.foot/]
