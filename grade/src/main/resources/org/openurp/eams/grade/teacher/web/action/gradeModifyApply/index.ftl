[#ftl]
[@b.head/]
[#include "./nav.ftl"/]
[@b.toolbar title="${b.text('info.courseList')}"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
  	[@b.form name="gradeModifyApplyIndexForm" action="!search" title="ui.searchForm" target="contentDiv"]
	<table class="indexpanel">
		<tr>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search" /]
			</td>
		</tr>
	</table>
	[/@]
[@b.foot/]
