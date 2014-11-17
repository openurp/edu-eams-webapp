[#ftl/]
[@b.head/]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="专业绩点排名统计"]
	bar.addBack("${b.text('action.back')}");
[/@]
<table class="indexpanel">
		<tr>
			<td class="index_view">
				[@b.form theme="search" action="!majorSearch" target="pageIframe" ]
					[@b.textfield label="attr.code" name="major.code" maxlength="20"/]
					[@b.textfield label="common.name" name="major.name"  maxlength="50"/]
					[@majorSelect id="s1" projectId="major.project.id" educationId="major.education.id" departId="major.department.id" /]
					[@b.select label="是否双专" name="major.project.minor" items={'0':'否','1':'是'} value="0"/]
				[/@]
			</td>
			<td class="index_content">
				[@b.div id="pageIframe" href="!majorSearch"/]
			</td>
		</tr>
</table>
[@b.foot/]
