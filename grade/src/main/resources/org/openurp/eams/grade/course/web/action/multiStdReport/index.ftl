[#ftl/]
[@b.head/]
[#include "nav.ftl" /]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="行政班每学期成绩表"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="stdSearchForm" action="!adminClassList" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="semester.id" value="${(semester.id)!}"/>
				[@b.textfield label="班级代码" name="adminclass.code" value="" maxlength="32" /]
			    [@b.textfield label="${b.text('std.grade')}" name="adminclass.grade" value="" maxlength="20" /]
	        	[@b.textfield label="${b.text('attr.name')}" name="adminclass.name" value="" maxlength="20" /]
				[@majorSelect id="s1" projectId="adminclass.project.id" educationId="adminclass.education.id" departId="adminclass.department.id" majorId="adminclass.major.id" directionId="adminclass.direction.id" stdTypeId="adminclass.type.id"/]
				[@b.select label='${b.text("common.status")}' name="enabled" items={'1':'${b.text("common.enabled")}','0':'${b.text("common.disabled")}'} value="1"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!adminClassList?semester.id=${semester.id!}" /]
			</td>
		</tr>
	</table>
[@b.foot/]
