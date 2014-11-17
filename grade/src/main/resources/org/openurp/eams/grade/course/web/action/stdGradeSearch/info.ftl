[#ftl]
[@b.head/]
[#assign gradeStatus={'0':'新添加','1':'已确认','2':'已发布'}/]
[#assign grade=courseGrade/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="学生单科成绩信息"]
   	bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable" width="100%">
	<tr>
     	<td class="title" width="15%">${b.text('attr.stdNo')}</td>
     	<td class="content" width="19%">${grade.std.code}</td>
     	<td class="title" width="15%">${b.text('attr.personName')}</td>
     	<td class="content" width="19%">[@i18nName grade.std/]</td>
     	<td class="title" width="15%">${b.text('department')}</td>
     	<td class="content">[@i18nName grade.std.department/]</td>
  	</tr>
  	<tr>
     	<td class="title">${b.text('attr.taskNo')}</td>
     	<td class="content">${grade.lessonNo?if_exists}</td>
     	<td class="title">${b.text('attr.courseNo')}</td>
     	<td class="content">${grade.course.code}</td>
     	<td class="title">${b.text('attr.courseName')}</td>
     	<td class="content">[@i18nName grade.course?if_exists/]</td>
  	</tr>
  	<tr>
     	<td class="title">学年学期</td>
     	<td class="content">${grade.semester.schoolYear}(${grade.semester.name})</td>
     	<td class="title">${b.text('attr.credit')}</td>
     	<td class="content">${(grade.course.credits)?if_exists}</td>
     	<td class="title">绩点</td>
     	<td class="content">${(grade.gp?string("#.##"))?if_exists}</td>
  	</tr>
  	<tr>
     	<td class="title">是否通过</td>
     	<td class="content">[#if grade.passed]是[#else]<font color="red">否[/#if]</td>
     	<td class="title">状态</td>
     	<td class="content">${gradeStatus[grade.status?default(0)?string]}</td>
     	<td class="title">${b.text('entity.markStyle')}</td>
     	<td class="content">[@i18nName grade.markStyle?if_exists /]</td>
  	</tr>
  	<tr>
     	<td class="title">得分</td>
     	<td class="content"><span [#if !grade.passed]style="color:red"[/#if]>${(grade.scoreText)!}</span></td>
     	<td class="title">${b.text('entity.courseType')}</td>
     	<td class="content">[@i18nName grade.courseType?if_exists/]</td>
     	<td class="title">修读类别</td>
     	<td class="content">[@i18nName grade.courseTakeType?if_exists/]</td>
  	</tr>
  	<tr>
	 	<td class="title">创建时间</td>
     	<td class="content">${(grade.createdAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
     	<td class="title">修改时间</td>
     	<td class="content">${(grade.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
     	<td class="title">学历层次</td>
     	<td class="content">${(grade.education.name)?if_exists}</td>
  	</tr>
</table>
[@b.grid items=grade.examGrades var="examGrade"]
	[@b.row]
		[@b.col width="20%" title="成绩种类"][@i18nName examGrade.gradeType/][/@]
		[@b.col width="10%" title="记录方式"][@i18nName examGrade.markStyle/][/@]
		[@b.col width="10%" title="考试情况"][@i18nName examGrade.examStatus/][/@]
		[@b.col width="10%" title="得分"]<span [#if !examGrade.passed] style="color:red" [/#if]>${(examGrade.scoreText)!}<span>[/@]
		[@b.col width="10%" title="百分比"][#if gradeState?exists]${(gradeState.getPercent(examGrade.gradeType)?string.percent)?if_exists}[/#if][/@]
		[@b.col width="10%" title="是否通过"][#if examGrade.passed]是[#else]<font color="red">否[/#if][/@]
		[@b.col width="10%" title="状态"]${gradeStatus[examGrade.status?string]}[/@]
		[@b.col width="20%" title="更新时间"]${(examGrade.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}[/@]
	[/@]
[/@]
[#--[@b.toolbar title="成绩修改日志"/]
[#include "../courseGradeAlterInfo.ftl"/]
--]
[@b.foot/]
