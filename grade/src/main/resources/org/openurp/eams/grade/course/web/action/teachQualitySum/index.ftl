[#ftl /]
[@b.head/]
[@b.toolbar title='质量分析管理' /]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester /]
<table class="indexpanel">
	<tr>
		<td class="index_view">
		[@b.form name="searchForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
		<input type='hidden' name="semester.id" id="semester.id" value="${semester.id}" />
			[@b.textfield name="lesson.no" label="序号" maxlength="32"/]
			[@b.textfield name="lesson.course.code" label="attr.courseNo" maxlength="32"/]
			[@b.textfield name="lesson.course.name" label="attr.courseName" maxlength="32"/]
			[@b.select label="状态" name="state" id="state" items={'0':'未提交','1':'已提交','2':'驳回修改'} value="1" /]
		[/@]
		</td>
		<td class="index_content">
			[@b.div href="!search?semester.id=${(semester.id)!}&state=1" id="contentDiv"/]
		</td> 
	</tr>
</table>
[@b.foot /] 
  
