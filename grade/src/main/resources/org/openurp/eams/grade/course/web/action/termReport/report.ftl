[#ftl/]
[@b.head/]
<style type="text/css">
.reportTable {
    border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
    font-family:宋体;
    font-size: 14px; 
    border-style: none;
    text-align:center;
}
table.reportTable td{
    border:solid;
    border-width:0px;
    border-right-width:1;
    border-bottom-width:1;
    border-color:#006CB2;
    border-left-width: 1px;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-right-width: 1px;
}
</style>
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"/]
[@b.toolbar title='每学期成绩打印']
	bar.addPrint();
	bar.addClose();
[/@]		
[#assign n = 1/]
 [#list stdGradeReports as report] 
 		<table width="95%" height="500px"><tr><td>
 		[@b.div style="margin-top:10px;text-align:center;font-size:18px;"]<strong>${report.std.project.school.name!}[@i18nName (report.std.type)!/]成绩单</strong>[/@]
 		[@b.div style="text-align:center;"](${semester.schoolYear}年度 [#if semester.name='1']第一学期[#elseif semester.name='2']第二学期[#else]${semester.name}[/#if])[/@]
 	 	[@b.div style="margin-top:10px;text-align:center;font-size:12px;"]
	 	 	${b.text("entity.department")}:[@i18nName report.std.department?if_exists/]
	 	 	${b.text("entity.major")}:[@i18nName report.std.major?if_exists/]
	 	 	${b.text("attr.stdNo")}:${report.std.code}
	 	 	${b.text("attr.personName")}：[@i18nName report.std/]
 	 	[/@]
 	 	[@b.div style="margin-top:10px;text-align:center;font-size:12px;"]
	 		选修学分:${report.electedCredit?if_exists}&nbsp;&nbsp;
	 		实修学分:${(report.credits)!}&nbsp;&nbsp;
	 		平均绩点:${(report.stdGpa.gpa)!}&nbsp;&nbsp;
	 		奖励学分:${report.awardedCredit?if_exists}
 	 	[/@]
 	 [@b.div style="margin-top:5px;"/]
     <table width="100%" align="center"  class="reportTable">
	   <tr>
	     <td width="35%">${b.text("attr.courseName")}</td>
	     <td width="10%">${b.text("attr.credit")}</td>
	     <td width="25%">性质</td>
	     <td width="10%">${b.text("field.exam.exam")}</td>
	     <td width="10%">绩点</td>
	     <td width="10%">备注</td>
	   </tr>	
	   [#list report.grades?if_exists as grade]
	   <tr> 
		   <td align="left">[@i18nName grade.course/]</td>
		   <td align="left">${grade.course.credits}</td>
		   <td align="left">[@i18nName grade.courseType?if_exists/]</td>	
		   <td align="left">
		   [#assign examStatus = ""/]
		   [#if !grade.getScoreText(setting.gradeType)??]
		   		[#if grade.getExamGrade(END)??]
		   			[#assign examStatus = (grade.getExamGrade(END)).examStatus.name/]
		   		[/#if]
		   [/#if]
		   	${grade.getScoreText(setting.gradeType)!examStatus}
		   </td>
		   <td align="left">${(grade.gp)!}</td>
		   <td align="left">&nbsp;</td>
	   </tr>
	   [/#list]
     </table>
     [@b.div style="margin-top:10px;"/]
     <table border="0" align="center" cellpadding="0" cellspacing="0" width="100%" style="border-color:white">
      	<tr valign="top">
      		<td style="border-color:white" id="contentValue">
      			${pcf!}
      		</td>
      	</tr>
      </table>
      </td>
      </tr>
      </table>
      [#if n==2 && stdGradeReports?size != (report_index+1)]
  			[#assign n=1/]
	      <div style="PAGE-BREAK-AFTER: always"></div>
	  [#else]
	        [#assign n = n+1/]
      [/#if]
[/#list]
[@b.foot /]