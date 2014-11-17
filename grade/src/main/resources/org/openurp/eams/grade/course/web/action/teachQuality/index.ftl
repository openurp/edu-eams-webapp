[#ftl /]
[@b.head/]
[@b.toolbar title='教学质量分析' /]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester /]
<table class="indexpanel">
	<tr>
		<td class="index_view">
		[@b.form name="searchForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
			<input type="hidden" name="lesson.semester.id" value="${semester.id}" />
			<input type="hidden" name="lesson.project.id" value="${Session['projectId']}" />
			
			[@b.textfield name="lesson.no" label="序号" maxlength="32"/]
			[@b.textfield name="lesson.course.code" label="attr.courseNo" maxlength="32"/]
			[@b.textfield name="lesson.course.name" label="attr.courseName" maxlength="32"/]
			[@b.select label="状态" name="state" items={'0':'未提交','1':'已提交','2':'待修改'} /]
		[/@]
		</td>
		<td class="index_content">
			[@b.div href="!search" id="contentDiv"/]
		</td> 
	</tr>
</table>
[@b.foot /] 
  
