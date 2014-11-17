[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>
[@b.toolbar title="院系成绩"]
	bar.addItem("不及格成绩", "noPassCourseGrades()");
	bar.addItem("无成绩学生名单", "noGradeTakes()");
	
    function noGradeTakes(){
    	var form = document.gradeIndexForm;
        bg.form.submit(form,"${b.url('!noGradeTakes')}","_blank");
    }
    function noPassCourseGrades(){
   		var form = document.gradeIndexForm;
        bg.form.submit(form,"${b.url('!unPassedGrades')}","_blank");
    }
[/@]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view" style="width:180px">
			[@b.form name="gradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="lesson.semester.id" value="${semester.id}"/>
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
				[#include "/com/ekingstar/eams/teach/grade/page/manager/common/taskBasicForm.ftl"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search" /]
			</td>
		</tr>
	</table>
<script>
jQuery(function() {
	bg.form.submit(document.gradeIndexForm);
});
</script>
[@b.foot/]
