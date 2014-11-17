[#ftl]
[@b.head/]
[@b.toolbar title="成绩修改申请审核"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="gradeAuditIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				[#include "searchForm.ftl"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search?applyStatus=ADMIN_AUDIT_PASSED" /]
			</td>
		</tr>
	</table>
[@b.foot/]
