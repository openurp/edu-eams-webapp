[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"/]
<style type="text/css">
.reportTable {
    border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
    font-family:仿宋_GB2312;
    font-size: 10pt; 
    border-style: none;
    font-size:14px;
    text-align:center;
}
table.reportTable td{
    border:solid;
    border-width:0px;
    border-color:#006CB2;
    border-left-width: 1px;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-right-width: 1px;
    font-size:12px;
}
table.reportTable tr{
	height:18px;
}
</style>
[@b.div id ="div1"][/@]
[@b.toolbar title="学生成绩打印"]
	bar.addPrint();
	bar.addClose();
[/@]
		[#assign allisprint = true/]
		[#list stds as std]
			<h3 style="text-align:center;">[@i18nName project.school/]学生成绩表</h3>
			[#assign lineCount = lineCountMap[std.code]/]
	    	[#assign semesters = stdMap[std.code]/]
	    	[#if stdGpaMap?exists]
		   		 [#assign stdGpa = stdGpaMap[std.code]!/]
		    [/#if]
		    [#assign stdSemesterName = stdSemesterNameMap[std.code]/]  
			[#assign isprint = true/]
		    [#assign stdSchoolYear = stdSemesetrMap[std.code]?sort/]	
		    [#if std.duration ==4]
		    	[#include "underGraduateModel.ftl"/]
			[#else]
			    [#include "specialistModel.ftl"/]
		    [/#if]
		    [@b.div style="margin-top:20px;"/]
	    [/#list]
[@b.foot/]