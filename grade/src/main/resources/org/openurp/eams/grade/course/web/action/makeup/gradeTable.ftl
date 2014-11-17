[#ftl/]
[@b.head/]
[#include "/template/print.ftl"/]
[#include "/template/macros.ftl"/]
<style>
.printTableStyle {
	border-collapse: collapse;
    border:solid;
	border-width:2px;
    border-color:#006CB2;
  	vertical-align: middle;
  	font-style: normal; 
	font-size: 10pt; 
}
table.printTableStyle td{
	border:solid;
	border-width:0px;
	border-right-width:2;
	border-bottom-width:2;
	border-color:#006CB2;
        height:26px;
}
</style>
[#macro emptyTd count]
     [#list 1..count as i]
     	<td></td>
     [/#list]
[/#macro]
[#assign pagePrintRow = 26 /]
<body>
   [#list examTasks as examTask ]
   [#assign examTakes= examTask?sort_by(['std','code'])/]
   [#assign boycount=0/]
   [#assign girlcount=0/]
    <br>
     <table width="100%" align="center" border="0"  >
	   <tr>
	    <td align="center" colspan="5" style="font-size:17pt" >
	     <B>缓补考成绩登记表</B>
	    </td>
	   </tr>
	   <tr><td colspan="5">&nbsp;</td></tr>
	 </table>	 
	 [#assign pageNos=(examTakes?size/(pagePrintRow*2))?int /]
	 [#if ((examTakes?size)>(pageNos*(pagePrintRow*2)))]
	 [#assign pageNos=pageNos+1 /]
	 [/#if]
	 [#list 0..pageNos-1 as pageNo]
	 [#assign passNo=pageNo*pagePrintRow*2 /]
	 <table width="100%" align="center" border="0"  >
	 <tr class="infoTitle">
	       <td >学年学期:${semester.schoolYear}学年 [#if semester.name='1']第一学期[#elseif semester.name='2']第二学期[#else]${semester.name}[/#if]</td>
		   <td >${b.text("attr.courseNo")}:${(examTakes[0].lesson.course.code)!}</td>
		   <td >${b.text("attr.courseName")}:[@i18nName (examTakes[0].lesson.course)!/]</td>
		   <td >${b.text("attr.teachDepart")}:[@i18nName (examTakes[0].lesson.teachDepart)!/]</td>
	 </tr>
	 </table>
	 <table class="gridtable" width="100%">
	   <tr class="gridhead" align="center">
	   	[#list 1..2 as i]
		     <td width="5%" >序号</td>
		     <td width="15%">${b.text("attr.stdNo")}</td>
		     <td width="10%">${b.text("attr.personName")}</td>	     
		     <td width="10%">成绩类型</td>
		     <td width="10%">成绩</td>
	     [/#list]
	   </tr>
	   [#list 0..pagePrintRow-1 as i]
	   <tr style="text-align:center" >
         [#if examTakes[i+passNo]?exists]
	     <td>${i+1+passNo}</td>
	     <td>${examTakes[i+passNo]?if_exists.std?if_exists.code}</td>
	     <td>[@i18nName examTakes[i+passNo]?if_exists.std?if_exists/]</td>
	     <td>[@i18nName examTakes[i+passNo]?if_exists.examType/]</td>
	     [@emptyTd count=1/]
         [/#if]
         
         [#if examTakes[i+pagePrintRow+passNo]?exists]
	     <td>${i+pagePrintRow+1+passNo}</td>
	     <td>${examTakes[i+pagePrintRow+passNo]?if_exists.std?if_exists.code}</td>
	     <td>[@i18nName examTakes[i+pagePrintRow+passNo]?if_exists.std?if_exists/]</td>
	     <td>[@i18nName examTakes[i+pagePrintRow+passNo]?if_exists.examType/]</td>
	     [@emptyTd count=1/]
         [#elseif examTakes[i+passNo]?exists]
          [@emptyTd count=5/]
         [/#if]

	   </tr>
	   [/#list]   
     </table>
     [#if pageNo_has_next]
 	 <div style='PAGE-BREAK-AFTER: always'></div>  
 	 [/#if]
 	 [#if !pageNo_has_next]	   
	   	 <table class="gridtable"  width="100%">
		    <tr  width="100%">
			   <td width="40%">
			   实际参加考试考查人数____________人<br>
			   缺&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;考&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;人&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数____________人<br>
			   教师签名:_______________________<br>
			   系主任签名:_____________________<br>
			   <br>
			   日期:_____________年_____月_____日
			   </td>
		     <td>
		       说明 :<br>
		      (1)学生缓考成绩以实际分数填写,学生补考成绩以及格或不及格填写,不及格成绩以红笔标记。<br>
		      (2)本表由阅卷教师填写,一式两份,一份由院系(部)汇总交教务处,一份留系部保存。 
		     </td>
		    </tr>
	  		</table>
	  		[#if examTask_has_next]
			 	  <div style='PAGE-BREAK-AFTER: always'></div>  
			[/#if]
	   [/#if] 
     [/#list]
   [/#list]
   <table width="100%" align="center">
	   <tr>
		   <td align="center">
		   	<input type="button" value="${b.text('action.print')}" onclick="print()" class="notprint">
		  </td>
	  </tr>
  </table>
 </body>
[@b.foot/]
