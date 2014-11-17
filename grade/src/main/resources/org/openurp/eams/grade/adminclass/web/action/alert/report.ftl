[#ftl/]
[@b.head/]
[@b.toolbar title="学业警告通知单打印"]
	bar.addPrint();
[/@]
[#assign pageRowCnt=0]
[#list gradeMap?keys?sort_by('code') as std]
	[#assign pageBreak=false/]
	[#--]固定行看做10行,每个A4纸最多48行[--]
	[#assign pageRowCnt =pageRowCnt+10+gradeMap.get(std)?size/]
	[#if pageRowCnt==48][#assign pageBreak=true/][#assign pageRowCnt = 0/]
	[#elseif pageRowCnt>48]
		<div style="PAGE-BREAK-AFTER: always"></div>
		[#assign pageRowCnt=10+gradeMap.get(std)?size/]
	[/#if]
	<br/>
	<table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
	    <tr>
	        <td style="font-weight:bold;font-size:14pt" height="30px">
	        ${semester.schoolYear}学年${semester.name}学期学生学业警告通知单
	       	<td>
	    </tr>
	</table>
	<table  style="width:90%;margin:auto" class="gridtable">
	<tr align="center"><td>姓名</td><td>${std.name}</td><td>学号</td><td>${std.code}</td><td>班级</td><td>${std.adminclass.name}</td></tr>
	</table>
	<table  style="width:90%;margin:auto" class="gridtable">
		<tr align="center"><td width="50%">不及格课程</td><td width="10%">成绩</td><td width="10%">学分</td><td width="30%">备注</td></tr>
		[#list gradeMap.get(std)?sort_by(['semester',"beginOn"]) as grade]
		<tr>
			<td>&nbsp;&nbsp;${grade.semester.schoolYear} ${grade.semester.name}学期 ${grade.course.name}</td>
			<td align="center">${grade.scoreText!}</td>
			<td align="center">${grade.course.credits}</td>
			[#if grade_index==0]<td rowspan="${gradeMap.get(std)?size}"></td>[/#if]
		</tr>
		[/#list]
	</table>
	<table  style="width:90%;margin:auto" class="gridtable">
	<tr>
		<td style="width:40px" align="center">学<br/>院<br/>审<br/>核</td>
		<td style="padding-left:10px">
		<table width="100%" cellpadding="0" cellspacing="0" style="border-width:0px"><tr><td style="border-width:0px;padding-top:5px;padding-bottom:5px">
		根据学籍管理条例规定，一学期经补考后的不及格课程学分累计达到或者超过10学分者，学校给予其学业警告。学生在校园期间不及格课程（含少修课程）
		学分累计达到30分（四年制本科为40分），学校劝其退学，特此通知。<br/>
		</td></tr><tr>
		<td align="right" style="border-width:0px">学院盖章 ${b.now?string("yyyy 年 MM 月 dd 日")}</td>
		</tr></table>
		</td>
	</tr>
	</table>
	[#if pageBreak]<div style="PAGE-BREAK-AFTER: always"></div>[/#if]
	
[/#list]
</div>