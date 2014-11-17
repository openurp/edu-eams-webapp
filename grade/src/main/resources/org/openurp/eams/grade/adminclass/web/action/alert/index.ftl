[#ftl/]
[@b.head/]
[#include "../components/nav.ftl"]

<div style="margin-left:20px">
[@b.form name="alertForm" action="!index"]
不及格学分下限:<input name="credits" value="${credits}" type="text"/>
<input name="adminclass.id" value="${adminclass.id}" type="hidden"/>
<input name="semester.id" value="${semester.id}" type="hidden"/>
[@b.submit value="查询"/]
[/@]

<table  style="width:70%"><tr><td>
<h3 style="margin-bottom:0px">共有${gradeMap?size}学生不及格学分等于或超过${credits}分</h3>
</td><td align="right" valign="bottom">
[#if gradeMap?size>0]<h3 style="margin-bottom:0px">[@b.a target="_blank" href="!report?adminclass.id=${adminclass.id}&semester.id=${semester.id}&credits=${credits}"]打印全部<image src="${b.theme.iconurl('actions/print.png')}" /> [/@]</h3>[/#if]
</td></tr></table>
	
[#list gradeMap?keys?sort_by('code') as std]
	<table  style="width:70%"><tr><td>
	<h5 style="margin-bottom:0px;margin-top:10px;">${std_index+1} ${std.code} ${std.name} ${std.adminclass.name} 不及格门数:${gradeMap.get(std)?size}&nbsp;&nbsp;</h5>
	</td><td align="right" valign="bottom">
	[@b.a target="_blank" href="!report?std.id=${std.id}&semester.id=${semester.id}&credits=${credits}"]<image width="14px"  title="打印" src="${b.theme.iconurl('actions/print.png')}" /> [/@]
	</td></tr></table>
	[@b.grid style="width:70%" items=gradeMap.get(std) var="grade" sortable="false"]
		[@b.row]
			[@b.col width="10%" title="序号"]${grade_index+1}[/@]
			[@b.col width="20%" title="学期"]${grade.semester.schoolYear}学年${grade.semester.name}学期[/@]
			[@b.col width="50%" title="课程名称" property="course.name"/]
			[@b.col width="10%" title="成绩" property="scoreText"/]
			[@b.col width="10%" title="学分" property="course.credits"/]
		[/@]
	[/@]
[/#list]
</div>