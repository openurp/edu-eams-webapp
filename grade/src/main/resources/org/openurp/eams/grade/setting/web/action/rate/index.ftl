[#ftl]
[@b.head/]
[#--
[#include "nav.ftl" /]
--]
[@b.toolbar title="成绩记录方式设置"/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="gradeRateConfigForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
	       		[@b.textfields names="gradeRateConfig.scoreMarkStyle.code;代码,gradeRateConfig.scoreMarkStyle.name;名称,gradeRateConfig.passScore;及格线" maxLength="20"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search" /]
			</td>
		</tr>
	</table>
[@b.foot/]
