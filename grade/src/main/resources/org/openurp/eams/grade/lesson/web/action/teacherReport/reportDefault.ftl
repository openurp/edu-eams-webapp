[#ftl]
[#include "/template/macros.ftl"/]
<#--这个是系统的打印模板-->
[@b.head/]
<style type="text/css">
.reportTable {
	border-collapse: collapse;
    border:solid;
	border-width:1px;
    border-color:#006CB2;
  	vertical-align: middle;
  	font-style: normal; 
	font-size: 10pt; 
}
table.reportTable td{
	border:solid;
	border-width:0px;
	border-right-width:1;
	border-bottom-width:1;
	border-color:#006CB2;
}
</style>
[@b.toolbar title="教学班成绩打印"]
	bar.addPrint();
   	bar.addItem("${b.text('action.export')}","exportData()");
   	bar.addBackOrClose();
[/@]
[#macro displayGrades(index,grade,gradeTypes)]
    <td style="text-align:center">${index+1}</td>
    <td>${grade.std.code}</td>
    <td>[@i18nName grade.std/]</td>
    [#list gradeTypes as gradeType]
		[#if gradeType.id=USUAL && (grade.courseTakeType.id)?if_exists=REEXAM]
     	<td>免修</td>
     	[#elseif grade.getExamGrade(gradeType)?exists]
	       	[#if !grade.getExamGrade(gradeType).examStatus.attended]
	        <td>[@i18nName grade.getExamGrade(gradeType).examStatus/]</td>
	       	[#else]
	        <td>${grade.getScoreInfo(gradeType)}</td>
	       	[/#if]
     	[#else]
     	<td style="text-align:center">${grade.getScoreInfo(gradeType)}</td>
     	[/#if]
    [/#list]
    <td></td>
[/#macro]
<div id = "DATA" width="100%" align="center" cellpadding="0" cellspacing="0">
[#assign pageSize=80]
[#list reports as report]
	[#assign grades=report.courseGrades]
	[#assign pages=(grades?size/pageSize)?int /]
	[#if grades?size==0][#break][/#if]
	[#if (pages*pageSize<grades?size)][#assign pages=pages+1][/#if]
	[#assign teachTask = report.task]
	[#list 1..pages as page]
		<div align='center'><h3>[@i18nName teachTask.project.school! /]课程成绩登记表</h3></div>
 	 	<div align='center'>${(teachTask.semester.schoolYear)!}年度${(teachTask.semester.name?replace('0','第'))?if_exists}学期</div>
 	 	<table width='100%' align='center' border='0' style="font-size:13px">
 	 		<tr>
	 	 		<td width='25%'>${b.text('attr.courseNo')}:${teachTask.course.code}</td>
	 	 		<td width='40%'>${b.text('attr.courseName')}:[@i18nName teachTask.course/]</td>
	 	 		<td align='left'>${b.text('entity.courseType')}:[@i18nName teachTask.courseType/]</td>
 	 		</tr>
 	 		<tr>
	 	 		<td>${b.text('attr.taskNo')}:${teachTask.seqNo?if_exists}</td>
	 	 		<td>${b.text('task.courseSchedule.primaryTeacher')}:[@getTeacherNames (teachTask.arrangeInfo.teachers)?if_exists/]</td>
	 	 		<td align='left'>授课院系:[@i18nName (teachTask.teachDepart)?if_exists/]</td>
 	 		</tr>	
 	 		<tr>
 	 	    	<td colspan="3">[#list report.gradeTypes as gradeType][#if gradeType.id != GA && gradeType.id != FINAL][#if teachTask.gradeState.getPercent(gradeType)?exists && teachTask.gradeState.getPercent(gradeType)?number != 0][@i18nName gradeType/]${teachTask.gradeState.getPercent(gradeType)?string.percent}　[/#if][/#if][/#list]</td>
 	 		</tr>
		</table>
		<table align="center" class="reportTable" style="table-layout: fixed">
			<tr align="center">
			[#list 1..2 as i]
	     		<td align="center" width="60px">${b.text('attr.index')}</td>
	     		<td align="center" width="90px">${b.text('attr.stdNo')}</td>
	     		<td width="150px">${b.text('attr.personName')}</td>
	     		[#list report.gradeTypes as gradeType]
	       		[#assign gradeTypeName][@i18nName gradeType/][/#assign]
	     		<td width="80px">${gradeTypeName?replace("总评成绩", "最终成绩")}</td>
	     		[/#list]
	     	<td align="center" width="80px">${b.text('attr.remark')}</td>
	   		[/#list]
	   		</tr>
	   		[#list 0..(pageSize/2-1) as i]
		   		<tr>
			   	[#assign j=i+(page-1)*pageSize]
			   	[#if grades[j]?exists]
					[@displayGrades j,grades[j],report.gradeTypes/]
			   	[#else]
			      	[#break]
			   	[/#if]
			   	[#assign j=i+(page-1)*pageSize+(pageSize/2)]
			   	[#if grades[j]?exists]
				    [@displayGrades j,grades[j],report.gradeTypes/]
			   	[#else]
			     	[#list 1..(4+report.gradeTypes?size) as i]
			       		<td>&nbsp;</td>
			     	[/#list]
			   	[/#if]
		   		</tr>
	   		[/#list]
     </table>
 	 <table width='100%' align='center' border='0' style="font-size:13px;vertical-align: center">
 	 	<tr height="30px">
	 	 	<td>任课教师签名:</td>
	 	 	<td width="15%">　　　年　　　月　　　日</td>
	 	 	<td width="5%"></td>
 	 	</tr>	
 	 </table>
     [#if page_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
     [/#list]
 	 [#if report_has_next]
	 	 <div style='PAGE-BREAK-AFTER: always'></div> 
 	 [/#if]
[/#list] 
</div>
<script>
   function exportData(){
       [#if Parameters['lessonIds']?exists]
       self.location="teachClassGradeReport!export.action?template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonIds']}";
       <#--该页面可能从单个成绩的录入跳转过来-->
       [#elseif Parameters['lessonId']?exists]
       self.location="teachClassGradeReport!export.action?template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonId']}";
       [/#if]
   }
</script>
[@b.foot/]
