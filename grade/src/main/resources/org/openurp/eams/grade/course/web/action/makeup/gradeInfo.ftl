[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#macro emptyTd count]
     [#list 1..count as i]
     <td></td>
     [/#list]
[/#macro]
[@b.toolbar title="补缓考成绩"]
	bar.addBack("${b.text("action.back")}");
[/@]
[#include "makeupReportMacros.ftl"]
[@makeupReportHead/]
	 [#assign examTakeList=examTakeList?sort_by(['std','code'])]
	 [#assign pageNo=(((examTakeList?size-1)/2)?int)]
	<table class="gridtable" style="width:100%;margin:auto" >
		<thead class="gridhead">
			<tr>
				[#list 1..2 as i]
					 <td width="5%" >序号</td>
				     <td width="15%">${b.text("attr.stdNo")}</td>
				     <td width="12%">${b.text("attr.personName")}</td>	     
				     <td width="10%">备注</td>
				     <td width="8%">成绩</td>
			    [/#list]
			</tr>
		</thead>
		<tbody>
	   [#assign examTakeList=examTakeList?sort_by(['std','code'])]
       [#assign pageNo=(((examTakeList?size-1)/2)?int)  /]
	   [#list 0..pageNo as i]
		   <tr style="text-align:center" >
		         [#if examTakeList[i]?exists]
				     <td>${i+1}</td>
				     <td>${examTakeList[i]?if_exists.std?if_exists.code}</td>
				     <td>[@i18nName examTakeList[i]?if_exists.std?if_exists/]</td>
				     <td>[@i18nName examTakeList[i]?if_exists.examType/]</td>
				     [#assign examGradeNo=examTakeList[i].id?string  /]
				     <td>${examGradeMap[examGradeNo]?if_exists.score?if_exists}</td>
		         [/#if]

		         [#assign nextExam=i+pageNo+1]
		         [#if examTakeList[nextExam]?exists]
				     <td>${nextExam+1}</td>
				     <td>${examTakeList[nextExam].std?if_exists.code}</td>
				     <td>[@i18nName examTakeList[nextExam].std?if_exists/]</td>
				     <td>[@i18nName examTakeList[nextExam].examType/]</td>
					 [#assign examGradeNo=examTakeList[nextExam].id?string  /]
				     <td>${examGradeMap[examGradeNo]?if_exists.score?if_exists}</td>
		         [#else]
		          [@emptyTd count=5/]
		         [/#if]
		   </tr>
	   [/#list]
	   [#if (examTakeList?size/2)<25]   
	   [#list 1..(25-examTakeList?size/2) as t]
       <tr>
           [#list 1..10 as i]
               <td>&nbsp;</td>
           [/#list]
       </tr>
       [/#list]
	   [/#if]
	  </tbody>
	 </table>
	[@makeupReportFoot/]
[@b.foot/]