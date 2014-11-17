[#ftl]
[@b.head/]
[#include "./nav.ftl"/]
[@b.toolbar title="${b.text('grade.teacher.modify.myApply')}"/]
	[@eams.semesterBar name="project.id" action="!myApply" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
  	[@b.form name="gradeModifyApplyIndexForm" action="!applyList" title="ui.searchForm" target="contentDiv"]
	<table class="indexpanel">
		<tr>
			<td class="index_content">
				[@b.div id="contentDiv" href="!applyList" /]
			</td>
		</tr>
	</table>
	[/@]
[@b.foot/]
