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

[#macro gaReportHead lesson]
<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:14pt" height="30px">
            [@i18nName lesson.project.school!/](${lesson.semester.schoolYear}学年${lesson.semester.name}学期)
            总评成绩登记表
           	</td>
        </tr>
    </table>
    <table width='95%' class="reportTitle" align='center' >
        <tr>
        		<td width="30%">课程名称:[@i18nName lesson.course!/][#if lesson.subCourse??]<sup>${lesson.subCourse.name}</sup>[/#if]</td>
            <td width="25%">${b.text("attr.courseNo")}:${lesson.course.code}</td>
            <td width="20%">${b.text("common.courseType")}:[@i18nName lesson.course.courseType!/]</td>
            <td width="15%">教师:[#list lesson.teachers as t]${t.name}&nbsp;[/#list]</td>
        </tr>
        <tr>
            <td>班级名称:
            [#assign len = (lesson.teachClass.name)?length/]
            [#assign teachclassName = lesson.teachClass.name!/]
            [#assign max = 14/]
            [#if len>max]
	            	${teachclassName?substring(0,max)}...
            [#else]
            	${teachclassName}
            [/#if]
            </td>
            <td>${b.text("attr.taskNo")}:${lesson.no}</td>
            <td>考核方式:[@i18nName lesson.examMode!/]</td>
            <td align="left">人数:${courseTakeMap.get(lesson)?size}</td>
        </tr>
        <tr>
        	<td align="left">院系:[@i18nName lesson.teachClass.depart!/]</td>
        	<td colspan="3">成绩类型:
        		[#list gradeTypes as gradeType][#if gradeType.id!=GA.id]&nbsp;${(gradeType.name)!}(__％)[/#if][/#list]
        	</td>
        </tr>
    </table>
[/#macro]

[#macro gaReportFoot(lesson)]
    <table align="center" class="reportFoot" width="95%">
    	<tr>
			<td width="20%">统计人数:${courseTakeMap.get(lesson)?size}</td>
			<td width="25%">总评平均成绩:</td>
			<td width="25%">教师签名:</td>
			<td width="30%">成绩录入日期:----年--月--日</td>
		</tr>
	</table>
[/#macro]

[#macro makeupReportHead lesson]
<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
        <tr>
            <td style="font-weight:bold;font-size:14pt" height="30px">
            [@i18nName lesson.project.school!/](${lesson.semester.schoolYear}学年${lesson.semester.name}学期)
            ${b.text('grade.makeupdelay')}登记表
           	</td>
        </tr>
    </table>
    <table width='95%' class="reportTitle" align='center'>
        <tr>
        	<td width="30%">课程名称:[@i18nName lesson.course!/]</td>
            <td width="25%">${b.text("attr.courseNo")}:${lesson.course.code}</td>
            <td width="20%">${b.text("attr.taskNo")}:${lesson.no}</td>
            <td width="15%">教师:[#list lesson.teachers as t]${t.name}&nbsp;[/#list]</td>
        </tr>
        <tr>
            <td>班级名称:
            [#assign len = (lesson.teachClass.name)?length/]
            [#assign teachclassName = lesson.teachClass.name!/]
            [#assign max = 14/]
            [#if len>max]
	            	${teachclassName?substring(0,max)}...
            [#else]
            	${teachclassName}
            [/#if]
            </td>
            <td>${b.text("common.courseType")}:[@i18nName lesson.courseType!/]</td>
            <td>考核方式:[@i18nName lesson.examMode!/]</td>
            <td align="left">人数:${(courseTakeMap.get(lesson)?size)!0}</td>
        </tr>
        <tr>
        	<td align="left">院系:[@i18nName lesson.teachDepart!/]</td>
        	<td colspan="3"></td>
        </tr>
    </table>
[/#macro]

[#macro gaColumnTitle]
<tr align="center" class="columnTitle">
         [#list 1..2 as i]
         <td class="columnIndexTitle" width="5%">${b.text("attr.index")}</td>
         <td width="15%">${b.text("attr.stdNo")}</td>
         <td width="8%">${b.text("attr.personName")}</td>
            [#list gradeTypes as gradeType]<td width="${22/gradeTypes?size}%">${gradeType.name}</td>[/#list]
         [/#list]
       </tr>
[/#macro]

[#macro makeupColumnTitle]
<tr align="center" class="columnTitle">
         [#list 1..2 as i]
         <td class="columnIndexTitle" width="5%">${b.text("attr.index")}</td>
         <td width="15%">${b.text("attr.stdNo")}</td>
         <td width="10%">${b.text("attr.personName")}</td>
         <td width="10%">成绩类型</td>
         <td width="10%">成绩</td>
         [/#list]
       </tr>
[/#macro]

[#macro makeupReportFoot (lesson)]
    <table align="center" class="reportFoot" width="95%">
    	<tr>
			<td width="20%">统计人数:${courseTakeMap.get(lesson)?size}</td>
			<td width="20%"></td>
			<td width="30%">教师签名:</td>
			<td width="30%">成绩录入日期:----年--月--日</td>
		</tr>
	</table>
[/#macro]

[#macro displayGaTake(courseTakes, objectIndex)]
[#if courseTakes[objectIndex]??]
    [#assign courseTake = courseTakes[objectIndex] /]
    <td class="columnIndex">${objectIndex + 1}</td>
    <td>${courseTake.std.code!}</td>
    <td style="font-size:11px">${courseTake.std.name!}[#if courseTake.courseTakeType?exists && courseTake.courseTakeType.id != 1]<sup>${courseTake.courseTakeType.name}</sup>[/#if]</td>
    [#list gradeTypes as gradeType]
    <td></td>
   	[/#list]
[#else]
    <td class="columnIndex"></td>
    <td></td>
    <td></td>
    [#list gradeTypes as gradeType]
    <td></td>
    [/#list]
[/#if]
[/#macro]

[#macro displayMakeupTake(courseTakes, objectIndex)]
[#if courseTakes[objectIndex]??]
    [#assign courseTake = courseTakes[objectIndex]/]
    <td class="columnIndex">${objectIndex + 1}</td>
    <td>${courseTake.std.code!}</td>
    <td>${courseTake.std.name!}[#if courseTake.courseTakeType?exists && courseTake.courseTakeType.id != 1]<sup>${courseTake.courseTakeType.name}</sup>[/#if]</td>
    <td>${stdExamTakeMap[courseTake.lesson.id?string+"_"+courseTake.std.id?string].examType.name}</td>
    <td></td>
[#else]
    <td class="columnIndex"></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
[/#if]
[/#macro]

[#include "blankMacroExt.ftl"/]