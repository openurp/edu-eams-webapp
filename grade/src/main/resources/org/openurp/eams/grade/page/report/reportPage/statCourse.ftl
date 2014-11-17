[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="成绩分段统计"]
	bar.addPrint();
	bar.addClose();
[/@]
<style type="text/Css">
.statTable{
  width:60%;
  align:center;
}
</style>
[#list courseStats as courseStat]
     [#assign course=courseStat.course]
 	 <div align='center'><h3>[@i18nName course.project.school! /]成绩分段统计表</h3></div>
 	 <div align='center'>${(courseStat.semester.schoolYear)?if_exists}学年 ${(courseStat.semester.name)?if_exists?replace('0','第')}学期</div><br> 	 
 	 <table class="gridtable" style="width:95%" align="center">
 	 	<tr>
 	 		<td width='20%'>课程代码:${course.code}</td>
 	 	    <td width='40%'>${b.text('attr.courseName')}:[@i18nName course/]</td>
 	 	</tr>
 	 </table>
 	 <table class="gridtable" style="width:95%" align="center">
 	    <tr>
 	    <td>
 	       分段依据:
 	    </td>
 	    [#list courseStat.scoreSegments as seg]
 	     <td>${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
 	    [/#list]
 	    </tr>
 	 </table>
   	<p></p>
	<table class="gridtable" style="width:95%" align="center">
     	<thead class="gridhead">
     		<tr>
     			<th></th>
			   	[#list courseStat.gradeSegStats as gradeStat]
			   		[#assign gradeTypeName]${gradeStat.gradeType.name!}[/#assign]
			   		<th colspan="2">
			   		${gradeTypeName}<br>
		   			<span style="font-weight:normal">人数(${gradeStat.stdCount})平均分:${(gradeStat.average?string("##.#"))?default('')} 最高分:${(gradeStat.heighest?string("##.#"))?default('')} 最低分:${(gradeStat.lowest?string("##.#"))?default('')}</span>
			   		</th>
	      		[/#list]
     		</tr>
     		<tr>
	      		<th>成绩段</th>
	      		[#list courseStat.gradeSegStats as gradeStat]
	      		<th>人数</th>
	      		<th>占总人数的比例</th>
	      		[/#list]
     		</tr>
     	</thead>
     	<tbody>
 		[#list courseStat.scoreSegments as seg]
     		<tr class="${(seg_index%2==0)?string('griddata-even','griddata-odd')}">
	     		<td>${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
	     		[#list courseStat.gradeSegStats as gradeStat]
	     			[#assign meSeg = gradeStat.scoreSegments[seg_index] /]
	     			<td>${meSeg.count}</td>
	     			<td>[#if gradeStat.stdCount?? && gradeStat.stdCount>0]${((meSeg.count/gradeStat.stdCount)*100)?string("##.#")}[#else]0.0[/#if]%</td>
	     		[/#list]
	     	</tr>
     	[/#list]
     	</tbody>
	</table>
    [#if courseStat_has_next]
    	<div style='PAGE-BREAK-AFTER: always'></div>
    [/#if]
[/#list]
[@b.foot/]
