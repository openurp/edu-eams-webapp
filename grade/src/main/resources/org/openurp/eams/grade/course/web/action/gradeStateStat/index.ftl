[#ftl]
[@b.head/]
[#include "/template/macros.ftl"]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/itemSelect.js"></script>
[@b.toolbar title="课程成绩统计"]
	bar.addHelp();
[/@]
[@eams.semesterBar name="project.id" target="contentDiv" action="!statusStat" semesterEmpty=false semesterName="semester.id" semesterValue=semester]
	<label for="departmentId">院系</label>
	<select id="departmentId" name="department.id" >
		<option>...</option>
		[#list departments as department]
			<option value="${department.id}">[@i18nName department/]</option>
		[/#list]
	</select>|
	<label for="gradeTypeId">成绩类型</label>
	<select id="gradeTypeId" name="gradeType.id" >
		<option>...</option>
		[#list gradeTypes as gradeType]
			[#if gradeType.id != finalId]
				<option value="${gradeType.id}" [#if GA_ID==gradeType.id]selected[/#if]>[@i18nName gradeType/]</option>
			[/#if]
		[/#list]
	</select>|
[/@]
<table class="indexpanel">
	<tr>
		<td class="index_content">
			[@b.div id="contentDiv" href="!statusStat?gradeType.id=${GA_ID}" /]
		</td>
	</tr>
</table>
[@b.foot/]
