[#ftl]
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
		height:20px;
	}
</style>
[@b.toolbar title="统计结果"]
	bar.addPrint();
	bar.addClose();
[/@]
    [#if results1?exists && results1?size != 0]
    	<h3 style="text-align:center;">[@i18nName course.project.school/]成绩分段统计表</h3>
        [#assign semesters = semesters?sort_by("id")/]
        <table align="center" width="95%">
        	<tr>
        		<td>
				    [@b.div style="font-size:11pt;"]
				    	${semesters?first.schoolYear!}学年第${semesters?first.name!}学期&nbsp;
				    	到 ${semesters?last.schoolYear!}学年第${semesters?last.name!}学期
				    [/@]
        		</td>
        	<tr>
        </table>
        [@b.div style="text-align:center"]课程名称:[@i18nName course/][/@]
	    <table align="center" width="80%" style="font-size:11pt" class="listTable">
	        <tr>
	            <td width="100px">分段依据：</td>
	            [#list sections as section]
	            <td>${(0 == section_index)?string("[", "(")}${section.fromScore!} - ${section.toScore!}]</td>
	            [/#list]
	        </tr>
	    </table>
	    [@b.div style="text-align:center;font-size:11pt"]
	    	最终成绩分段统计表　人次数：(${results2?first[0]!})　平均分：${results2?first[1]!}　最高分：${results2?last[2]!}　最低分：${results2?last[3]!}
	    [/@]
	    <table class="reportTable" width="98%" style="text-align:center" align="center">
	        <tr class="darkColumn">
	            <td width="" rowspan="2">成绩段</td>
	            [#list semesters as semester]
	            <td width="" colspan="2">${semester.schoolYear!} ${semester.name!}</td>
	            [/#list]
	        </tr>
	        <tr class="darkColumn">
	            [#list semesters as semester]
	            <td>人数</td>
	            <td>占总人数的比例</td>
	            [/#list]
	        </tr>
	        [#list sections as section]
	        <tr>
	            <td>${(0 == section_index)?string("[", "(")}${section.fromScore!} - ${section.toScore!}]</td>
	            [#if results1[0][0] != semesters[0].id]
	            <td>0</td>
	            <td>0%</td>
	            [/#if]
	            [#list results1 as result]
	                [#if result[0] != semesters[result_index].id]
	            <td>0</td>
	            <td>0%</td>
	                [/#if]
	            <td>${result[section_index + 2]}</td>
	            <td>${(result[section_index + 2] / result[1] * 100)}%</td>
	            [/#list]
	            [#if results1[results1?size - 1][0] != semesters[semesters?size - 1].id]
	            <td>0</td>
	            <td>0%</td>
	            [/#if]
	        </tr>
	        [/#list]
	    </table>
	 [#else]
	 没有成绩或没有符合条件的记录!
    [/#if]
[@b.foot/]