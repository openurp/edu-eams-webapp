[#ftl]
[@b.head/]
[#include "nav.ftl" /]
	[@b.toolbar title="成绩录入开关设置"]
		bar.addBlankItem();
	[/@]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="gradeSwForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				[@b.select items={'1':'开放','0':'关闭'} label='开关状态' empty="全部" name="gradeInputSwitch.opened"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search" /]
			</td>
		</tr>
	</table>
[@b.foot/]
