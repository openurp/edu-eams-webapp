[#ftl] 
[@b.head/]
[@b.toolbar title="${b.text('info.courseList')}"]
[/@]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
  	[@b.form name="teacherGradeIndexForm" action="!taskList" title="ui.searchForm" target="contentDiv"]
	<table class="indexpanel">
		<tr>
			<td class="index_content">
				[@b.div id="contentDiv" href="!taskList?lesson.semester.id=${(semester.id)?default('')}" /]
			</td>
		</tr>
	</table>
	[/@]
[@b.foot/]
