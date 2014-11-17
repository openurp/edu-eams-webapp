[#ftl]
[@b.head/]
[#include "nav.ftl"/]
[@b.toolbar title="教学班成绩打印"]
	bar.addBlankItem();
[/@]	
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
<table class="indexpanel">
	<tr>
		<td class="index_view">
		[@b.form name="gradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
			<input type="hidden" name="lesson.semester.id" value="${semester.id!}"/>
			[#include "../components/taskBasicForm.ftl"/]
		[/@]
	   	</td>
		<td class="index_content">
			[@b.div id="contentDiv" href="!search?lesson.semester.id=${semester.id!}" /]
		</td>
	</tr>
</table>
[@b.foot/]
