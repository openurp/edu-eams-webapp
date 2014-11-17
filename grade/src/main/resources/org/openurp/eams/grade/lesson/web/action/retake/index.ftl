[#ftl]
[@b.head/]
[@b.toolbar title="重修课程统计"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form  action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="semester.id" value="${semester.id}"/>
				[@b.textfields  names="course.code;attr.courseNo,course.name;attr.courseName"/]
				[@b.select  label="attr.teachDepart" name="course.department.id" items=(departmentList)?sort_by("code") empty="${b.text('common.all')}"/]
			[/@]
			</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search?semester.id=${semester.id}"/]
			</td>
		</tr>
	</table>
[@b.foot/]