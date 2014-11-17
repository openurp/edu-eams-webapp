[#ftl]
[#include "/template/macros.ftl"/]
[#assign _pageSize=50/]
<style>
.tableStyle { font-size:10px; border-collapse:collapse; width:95% }
.tableStyleTitle{font-weight:bold;}
.tableStyleTd { border: solid #000 1px; text-align:center}
</style> 
[#list students as std]
<h2 style="text-align:center;">[@i18nName school/]${b.text('common.studyGradeTable')}</h2>
<table align="center" style="width:95%; font-size:10px">
 	<tr>
 		<td>${b.text('entity.department')}:[@i18nName std.department!/]</td>
 		<td colspan="2">${b.text('entity.major')}(${b.text('entity.direction')}):[@i18nName std.major!/]</td>
 		<td width="20%">${b.text('entity.adminClass')}:[@i18nName std.adminclass!/]</td>
 		<td width="15%"></td>
 	</tr>
 	<tr>
 		<td>${b.text('attr.eduLength')}:${std.duration!}</td>
 		<td>${b.text('attr.stdNo')}:${std.code!}</td>
 		<td>${b.text('attr.personName')}:[@i18nName std! /]</td>
 		<td>${b.text('attr.gender')}:[@i18nName std.gender! /]</td>
 		<td>${b.text('common.tutor')}:</td>
 	</tr>
</table>

<table class="tableStyle" align="center">
  	<tr align="center" class="tableStyleTitle">
  		[#list 1..2 as i]
	         <td class="tableStyleTd" width="26%">${b.text('课程名称')}</td>
	         <td class="tableStyleTd" width="7%" align="center">${b.text('attr.credit')}</td>
	         <td class="tableStyleTd" width="7%">${b.text('grade.score')}</td>
	         <td class="tableStyleTd" width="10%">${b.text('field.teacherEvaluate.term')}</td>
  		[/#list]
  	</tr>
  	
  	[#list 0.._pageSize-1 as i]
  	[#assign stdGrades = grades.get(std)]
	<tr>
		[#if stdGrades[i]??]
		[#assign courseGrade = stdGrades[i] /]
		<td class="tableStyleTd">[@i18nName courseGrade.course! /]</td>
		<td class="tableStyleTd">${courseGrade.course.credits!}</td>
		<td class="tableStyleTd">${courseGrade.scoreText!}</td>
		<td class="tableStyleTd">${courseGrade.semester.schoolYear!}(${courseGrade.semester.name!})</td>
		[#else]
		<td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td>
		[/#if]
		
		[#if stdGrades[i+_pageSize]??]
		[#assign courseGrade = stdGrades[i+_pageSize] /]
		<td class="tableStyleTd">[@i18nName courseGrade.course! /]</td>
		<td class="tableStyleTd">${courseGrade.course.credits!}</td>
		<td class="tableStyleTd">${courseGrade.scoreText!}</td>
		<td class="tableStyleTd">${courseGrade.semester.schoolYear!}(${courseGrade.semester.name!})</td>
		[#else]
		<td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td><td class="tableStyleTd">&nbsp;</td>
		[/#if]
	</tr>
  	[/#list]
</table>

[@b.div style="margin-top:5px;"/]

<table align="center" style="width:95%; font-size:10px">
	<tr>
		<td colspan="2">${b.text('grade.creditTotal')}:${(gpas.get(std).credits)!}</td>
        <td colspan="2">${b.text('filed.averageScoreNod')}:${(gpas.get(std).gpa)!}</td>
    </tr>
    <tr><td colspan="4">&nbsp;</td></tr>
    <tr>
      <td width="25%">制表人：${(_printBy)!}</td>
      <td width="35%">学院（系）签章：&nbsp;</td>
      <td width="20%">教务处签章：&nbsp;</td>
      <td width="20%">${b.text('common.printDate')}:${(b.now?string('yyyy-MM-dd'))!}</td>
   </tr>
</table>
<div style="PAGE-BREAK-AFTER: always"></div>
[/#list]