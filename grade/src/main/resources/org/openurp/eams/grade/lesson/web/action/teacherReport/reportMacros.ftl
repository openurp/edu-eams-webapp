[#ftl]
[#macro reportStyle]
<style type="text/css">
.reportBody {
    border:solid;
    border-color:#006CB2;
    border-collapse: collapse;
    border-width:2px;
    vertical-align: middle;
    font-style: normal; 
    font-size: 13px; 
    font-family:宋体;
    table-layout: fixed;
    text-align:center;
}
table.reportBody td{
    border-style:solid;
    border-color:#006CB2;
    border-width:0 1px 1px 0;
}

table.reportBody td.columnIndex{
    border-width:0 1px 1px 2px;
}

table.reportBody tr{
	height:20px;
}
table.reportTitle tr{
	height:20px;
	border-width:1px;
	font-size:13px;
}
tr.columnTitle td{
	border-width:1px 1px 2px 1px;
}

tr.columnTitle td.columnIndexTitle{
	border-width:1px 1px 2px 2px;
	font-size:12px;
}

table.reportFoot{
	margin-bottom:20px;
}
table.reportFoot.tr {
}
.examStatus{
	font-size:10px;
}
.longScoreText{
	font-size:10px;
}
</style>

[/#macro]

[#macro gaReportHead report]
<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:14pt" height="30px">
            [@i18nName report.lesson.project.school!/](${(report.lesson.semester.schoolYear)?if_exists}学年${(report.lesson.semester.name)?if_exists?replace("0","第")}学期)
            总评成绩登记表
           	<td>
        </tr>
    </table>
    <table width='95%' class="reportTitle" align='center'>
        <tr>
        	<td width="30%">课程名称:[@i18nName report.lesson.course!/]</td>
            <td width="25%">${b.text("attr.courseNo")}:${report.lesson.course.code}</td>
            <td width="20%">${b.text("common.courseType")}:[@i18nName report.lesson.course.courseType!/]</td>
            <td width="15%">教师:[#list report.lesson.teachers as t]${t.name}&nbsp;[/#list]</td>
        </tr>
        <tr>
            <td>班级名称:
            [#assign len = (report.lesson.teachClass.name)?length/]
            [#assign adminclassName = report.lesson.teachClass.name!/]
            [#assign max = 28/]
            [#if len>max]
	            [#list 0..(len/max-1) as i]
	            	${adminclassName?substring(i*max,i*max+max)}<br>
	            [/#list]
	            [#if (len%max !=0)]
	            	${adminclassName?substring(len-(len%max),len)}
	            [/#if]
            [#else]
            	${adminclassName}
            [/#if]
            </td>
            <td>${b.text("attr.taskNo")}:${report.lesson.no}</td>
            <td>考核方式:[@i18nName report.lesson.examMode!/]</td>
            <td align="left">人数:${(report.courseGrades?size)!0}</td>
        </tr>
        <tr>
        	<td align="left">院系:[@i18nName report.lesson.teachClass.depart!/]</td>
        	<td colspan="3">成绩类型:
        		[#list report.gradeTypes as gradeType]
	        	[#if (report.courseGradeState.getState(gradeType).percent)??]&nbsp;${(gradeType.name)!}(${report.courseGradeState.getState(gradeType).percent * 100}％)[/#if]
        		[/#list]
        	</td>
        </tr>
    </table>
[/#macro]

[#macro gaReportFoot report]
    <table align="center" class="reportFoot" width="95%">
    	<tr>
			<td width="20%">统计人数:${totalNormal!0}</td>
			<td width="25%">总评平均成绩:[#if totalNormal>0]${totalNormalScore/totalNormal}[/#if]</td>
			<td width="25%">教师签名:</td>
			<td width="30%">成绩录入日期:${(report.courseGradeState.getState(GA).inputedAt?string('yyyy-MM-dd'))!}</td>
		</tr>
	</table>
[/#macro]

[#macro makeupReportHead report]
<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:14pt" height="30px">
            [@i18nName report.lesson.project.school!/](${(report.lesson.semester.schoolYear)?if_exists}学年${(report.lesson.semester.name)?if_exists?replace("0","第")}学期)
            补(缓)考成绩登记表
           	<td>
        </tr>
    </table>
    <table width='95%' class="reportTitle" align='center'>
        <tr>
        	<td width="30%">课程名称:[@i18nName report.lesson.course!/]</td>
            <td width="25%">${b.text("attr.courseNo")}:${report.lesson.course.code}</td>
            <td width="20%">${b.text("common.courseType")}:[@i18nName report.lesson.course.courseType!/]</td>
            <td width="15%">教师:[#list report.lesson.teachers as t]${t.name}&nbsp;[/#list]</td>
        </tr>
        <tr>
            <td>班级名称:
            [#assign len = (report.lesson.teachClass.name)?length/]
            [#assign adminclassName = report.lesson.teachClass.name!/]
            [#assign max = 28/]
            [#if len>max]
	            [#list 0..(len/max-1) as i]
	            	${adminclassName?substring(i*max,i*max+max)}<br>
	            [/#list]
	            [#if (len%max !=0)]
	            	${adminclassName?substring(len-(len%max),len)}
	            [/#if]
            [#else]
            	${adminclassName}
            [/#if]
            </td>
            <td>${b.text("attr.taskNo")}:${report.lesson.no}</td>
            <td>考核方式:[@i18nName report.lesson.examMode!/]</td>
            <td align="left">人数:${(report.courseGrades?size)!0}</td>
        </tr>
        <tr>
        	<td align="left">院系:[@i18nName report.lesson.teachClass.depart!/]</td>
        	<td colspan="3"></td>
        </tr>
    </table>
[/#macro]

[#macro reportColumnTitle report]
<tr align="center" class="columnTitle">
         [#list 1..2 as i]
         <td class="columnIndexTitle" width="5%">${b.text("attr.index")}</td>
         <td width="15%">${b.text("attr.stdNo")}</td>
         <td width="8%">${b.text("attr.personName")}</td>
            [#list report.gradeTypes as gradeType]
            [#assign gradeTypeName]${gradeType.name!}[/#assign]<td width="${22/report.gradeTypes?size}%">${gradeTypeName}</td>
            [/#list]
         [/#list]
       </tr>
[/#macro]

[#macro makeupReportFoot report]
    <table align="center" class="reportFoot" width="95%">
    	<tr>
			<td width="20%">统计人数:${report.courseGrades?size}</td>
			<td width="20%"></td>
			<td width="30%">教师签名:</td>
			<td width="30%">成绩录入日期:${(report.courseGradeState.inputedAt?string('yyyy-MM-dd'))!}</td>
		</tr>
	</table>
[/#macro]

[#macro displayGaGrade(report, objectIndex)]
[#if report.courseGrades[objectIndex]??]
    [#assign courseGrade = report.courseGrades[objectIndex]/]
    <td class="columnIndex">${objectIndex + 1}</td>
    <td>${courseGrade.std.code!}</td>
    <td style="font-size:11px">${courseGrade.std.name!}[#if courseGrade.courseTakeType?exists && courseGrade.courseTakeType.id != 1]<sup>${courseGrade.courseTakeType.name}</sup>[/#if]</td>
    
    [#list report.gradeTypes as gradeType]
    <td>
    [#local examGrade=courseGrade.getExamGrade(gradeType)!"null"/]
    [#if examGrade!="null"]
    [#if !examGrade.markStyle.numStyle && (examGrade.scoreText!)?length>2]<span class="longScoreText">${examGrade.scoreText!}</span>[#else]${examGrade.scoreText!}[/#if][#if examGrade.examStatus?? && examGrade.examStatus.id!=1]<span class="examStatus"> ${examGrade.examStatus.name}</span>[/#if]
    [/#if]
    </td>
   	[/#list]
[#else]
    <td class="columnIndex"></td>
    <td></td>
    <td></td>
    [#list report.gradeTypes as gradeType]
    <td></td>
    [/#list]
[/#if]
[/#macro]

[#macro displayMakeupGrade(report, objectIndex)]
[#if report.courseGrades[objectIndex]??]
    [#assign courseGrade = report.courseGrades[objectIndex]/]
    <td class="columnIndex">${objectIndex + 1}</td>
    <td>${courseGrade.std.code!}</td>
    <td>${courseGrade.std.name!}[#if courseGrade.courseTakeType?exists && courseGrade.courseTakeType.id != 1]<sup>${courseGrade.courseTakeType.name}</sup>[/#if]</td>
    
    [#list report.gradeTypes as gradeType]
    <td>
    [#if gradeType.id==FINAL.id]${courseGrade.scoreText!}[#else]
    [#local examGrade=courseGrade.getExamGrade(gradeType)!"null"/]
    [#if examGrade!="null"]
    [#if !examGrade.markStyle.numStyle && (examGrade.scoreText!)?length>2]<span class="longScoreText">${examGrade.scoreText!}</span>[#else]${examGrade.scoreText!}[/#if][#if examGrade.examStatus?? && examGrade.examStatus.id!=1]<span class="examStatus"> ${examGrade.examStatus.name}</span>[/#if]
    [/#if]
    [/#if]
    </td>
   	[/#list]
[#else]
    <td class="columnIndex"></td>
    <td></td>
    <td></td>
    [#list report.gradeTypes as gradeType]
    <td></td>
    [/#list]
[/#if]
[/#macro]

[#include "reportMacroExt.ftl"/]