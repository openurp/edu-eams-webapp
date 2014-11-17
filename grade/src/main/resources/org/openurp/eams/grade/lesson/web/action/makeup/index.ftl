[#ftl]
[@b.head/]
[@b.toolbar title="${b.text('grade.slowUpResultsManagement')}"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view" style="width:170px">
				[@b.form name="makeupGradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
					<input type="hidden" value="${semester.id}" name="examTake.semester.id"/>
					[@b.textfield name="examTake.lesson.course.code" label="attr.courseNo"/]
					[@b.textfield name="examTake.lesson.course.name" label="attr.courseName"/]
					[@b.select label="${b.text('attr.teachDepartMent')}" id="teachDepartId" name="examTake.lesson.teachDepart.id" items=teachDepartList empty="..."/]
				[/@]
			</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search?examTake.semester.id=${(semester.id)?default('')}" /]
			</td>
		<tr>
	</table>
[@b.foot/]  
