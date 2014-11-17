[#ftl/]
[@b.head/]
[@b.toolbar title="学生在校成绩统计"]
	bar.addBack("${b.text('action.back')}");
[/@]	
[#list stdGpas as stdGpa]
	[#include "stdGradeStat.ftl"/]
[/#list]
[@b.foot/]
