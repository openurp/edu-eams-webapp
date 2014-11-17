[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"/]
[@b.toolbar title="学生个人成绩单"]
	bar.addPrint();
	bar.addBackOrClose();
[/@]
	[@b.div style="margin-top:10px"/]
	<center>
 	<table class="gridtable" style="width:60%;text-align:left;">
 		<tr>
 			<td>${b.text('attr.stdNo')}:</td>
 			<td>${(std.code)!}</td>
 		</tr>
 		<tr>
 			<td>${b.text('attr.personName')}:</td>
 			<td>[@i18nName std/]</td>
 		</tr>
 		<tr>
 			<td>${b.text('entity.adminClass')}:</td>
 			<td>[@i18nName std.adminclass?if_exists/]</td>
		</tr>
 		<tr>
 			<td>${b.text('entity.direction')}:</td>
 			<td>[@i18nName std.direction?if_exists/]</td>
 		</tr>
 	</table>
 	[@b.div style="margin-top:5px;"/]
 	<table width="95%" align="center">
		<tr>
			<td>
			 	[@b.grid items=courseGrades?sort_by(["semester","beginOn"]) var="grade"]
			 		[@b.row]
			 			[@b.col width="10%" title="attr.taskNo"]${(grade.lessonNo)!}[/@]
			 			[@b.col width="10%" title="attr.courseNo"]${(grade.course.code)!}[/@]
			 			[@b.col width="20%" title="attr.courseName"][@i18nName grade.course/][#if grade.courseTakeType?? && grade.courseTakeType.id == RESTUDY]<span style="background-color:red;"></span>(重修)[/#if][/@]
			 			[@b.col width="20%" title="common.courseType"]${(grade.courseType.name)!}[/@]
			 			[@b.col width="20%" title="学年度"]${(grade.semester.schoolYear)!}[/@]
			 			[@b.col width="10%" title="学期"]${(grade.semester.name)?if_exists?replace('0','第')}[/@]
			 			[@b.col width="10%" title="成绩"]${(grade.scoreText)?if_exists}[/@]
			 		[/@]
			 	[/@]
		 	</td>
	 	</tr>
 	</table>
[@b.foot/]