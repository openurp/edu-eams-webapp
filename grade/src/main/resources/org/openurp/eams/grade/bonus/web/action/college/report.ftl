[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
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
	font-size:14px;
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
[@b.toolbar title="加分表打印"]
	 bar.addPrint();
	 bar.addClose();
[/@]
	[#assign perRecordOfPage = 22/]
    [#list examGradeMap?keys as department]
	[#assign index=0/]
 	[@printMore examGradeMap,semester,department,index,perRecordOfPage/]
	    [#if examGradeMap.get(department)?size > perRecordOfPage]
	    	[#assign mapSizeRate = examGradeMap.get(department)?size/perRecordOfPage]
	    	[#assign listNum = (examGradeMap.get(department)?size/perRecordOfPage)?int/]
	    	[#if mapSizeRate > listNum]
	    		[#assign listNum = listNum +1/]
	    	[/#if]
	    	[#list 1..(listNum-1) as tabIndex]
	    		[@printMore examGradeMap,semester,department,index,perRecordOfPage/]
	    		[#assign index = index + 22/]
	    	[/#list]
	    [/#if]
    [/#list]
[#macro printMore(examGradeMap,semester,department,index,perRecordOfPage)]
	[#assign listIndex = index/]
    	<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
	        <tr>
	            <td style="font-weight:bold;font-size:15pt" height="40px">
	            	${(semester.schoolYear)?if_exists}学年第${(semester.name)?if_exists?replace("0","")}学期[@i18nName project.school!/]加分成绩表
	           	</td>
	        </tr>
	    </table>
	    <br/>
	    <table align="center" style="text-align:center;width:95%" cellpadding="0" cellspacing="0">
	        <tr>
	            <td align="left" height="40px">
	            	开课院系名:[@i18nName department/]
	           	</td>
	        </tr>
	    </table>
	    <table align="center" class="reportBody" width="95%">
			<tr align="center" style="height:32px">
	         	<td width="10%">${b.text("attr.stdNo")}</td>
	         	<td width="15%">${b.text("attr.studentName")}</td>
	         	<td width="15%">${b.text("attr.taskNo")}</td>
	         	<td width="40%">${b.text("filed.courseName")}</td>
	         	<td width="15%">${b.text("filed.teacherName")}</td>
	         	<td width="5%">加分</td>
	       </tr>
			[#list 0..(perRecordOfPage- 1) as onePageRecordIndex]
			[#if (examGradeMap.get(department)[listIndex])??]
				[#assign examGrade = examGradeMap.get(department)[listIndex]/]
				[#assign listIndex = listIndex +1/]
			[#else]
				[#assign examGrade = emptyExamGrade/]
			[/#if]
			<tr style="height:32px">
				<td>${(examGrade.courseGrade.std.code)!}</td>
	         	<td>${(examGrade.courseGrade.std.name)!}</td>
	         	<td>${(examGrade.courseGrade.lesson.no)!}</td>
	         	<td>${(examGrade.courseGrade.course.name)!}</td>
	         	<td>[@getTeacherNames (examGrade.courseGrade.lesson.teachers)!/]</td>
	         	<td>${(examGrade.scoreText)!}</td>
	       	</tr>
	       	[/#list]
	    </table>
	    <br/>
	    <table align="center" style="text-align:center;width:95%" cellpadding="0" cellspacing="0">
	        <tr style="height:30px">
	            <td align="left" width="70%">
	            	体育教学部负责人签字:_______________________
	           	</td>
	           	<td align="left" width="30%">
	           		打印日期：${sysDate?string("yyyy年MM月dd日")}
	           	</td>
	        </tr>
	        <tr style="height:30px">
	        	<td>
	        	</td>
	        	<td align="left">
	        		<br/>
	        		体育教学部(章)：
	        	</td>
	        </tr>
	    </table>
	    <div style="PAGE-BREAK-AFTER: always"></div>
[/#macro]
[@b.foot/]