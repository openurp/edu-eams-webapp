[#ftl/]
[@b.head/]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="班级绩点排名统计"]
	bar.addBack("${b.text("action.back")}");
[/@]
		<table class="indexpanel">
				<tr>
					<td class="index_view">
						[@b.form theme="search" name="actionForm" target="pageIframe" action="!adminclassSearch"]
							[@b.textfield label="班级代码" name="adminclass.code" maxlength="20"/]
							[@b.textfield label="std.grade" name="adminclass.grade" maxlength="7"/]
							[@b.textfield label="attr.name" name="adminclass.name" maxlength="20"/]
							[@majorSelect id="s1" projectId="adminclass.project.id" educationId="adminclass.education.id" departId="adminclass.department.id" majorId="adminclass.major.id" directionId="adminclass.direction.id" stdTypeId="adminclass.stdType.id"/]
							[@b.select label="是否双专" name="adminclass.major.project.minor" items={'0':'否','1':'是'} value="0"/]
							[@b.select label="common.status" name="adminclass.enabled" items={'1':'${b.text("common.enabled")}','0':'${b.text("common.disabled")}'} value="1"/]
						[/@]
					</td>
					<td class="index_content">
						[@b.div id="pageIframe" href="!adminclassSearch?adminclass.major.project.minor=0"/]
					</td>
				</tr>
		</table>
[@b.foot/]