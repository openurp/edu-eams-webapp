[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]

[#list courseStats as courseStat]
     <table style="width:95%" align="center">
     	<tr>
     		<td>一.	平时/期中/期末试卷成绩统计（考试情况为正常的数据统计）：</td>
     	</tr>
     </table>
 	 <table class="gridtable" style="width:95%" align="center">
 	    <tr>
 	    	<td align="center" width="10%" nowrap="true">分段依据</td>
 	    [#list courseStat.scoreSegments as seg]
 	     <td align="center" width="10%" nowrap="true">${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
 	    [/#list]
 	    <td align="center" width="10%" nowrap="true">优秀率</td>
 	    <td align="center" width="10%" nowrap="true">及格率</td>
 	    <td align="center" width="15%"  nowrap="true">班平均成绩</td>
 	    </tr>
 	    [#list courseStat.gradeSegStats?sort_by(["gradeType","code"]) as gradeStat]
 	     <tr>
 	    	<td align="center">${gradeStat.gradeType.name!}</td>
 	    [#assign excellent=0/]
 	    [#assign passed=0/]
 	    [#list gradeStat.scoreSegments as seg]
 	     <td align="center">
 	     ${seg.count}/${((seg.count/gradeStat.stdCount)*100)?string("##.#")}%
 	     [#if seg.min>60 || seg.min=60][#assign passed = passed+seg.count /] [/#if]
 	     [#if seg.min>90 || seg.min=90][#assign excellent=excellent+seg.count/][/#if]
 	     </td>
 	     [/#list]
 	     <td align="center">[#if gradeStat.stdCount>0]${(excellent/gradeStat.stdCount*100)?string("##.#")}%(${excellent}/${gradeStat.stdCount})[/#if]</td>
 	     <td align="center">[#if gradeStat.stdCount>0]${(passed/gradeStat.stdCount*100)?string("##.#")}%(${passed}/${gradeStat.stdCount})[/#if]</td>
 	     <td align="center">${gradeStat.average!}</td>
 	    </tr>
 	    [/#list]
 	</table>
	[/#list]
[@b.foot/]
