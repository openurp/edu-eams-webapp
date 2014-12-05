[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="grade.stdPersonScoreSearch"]
	   function displayGrade(form) {
	     if(form['semester.id'].value!='') {
	        bg.form.submit(form)
	     }else{
	        bg.form.submit(form,"${b.url('!history')}");
	     }
	   }
	   function personGrade(form){
		   	form.target="_blank";
		   	bg.form.submit(form,"${base}/teach/grade/transcript/report.action");
	   		form.target="allSemesterScoreForm";
	   }
[/@]
[@b.form name="allSemesterForm" action="!index"]
学年学期:[@b.select name="semester.id" option=r"${(item.schoolYear)!} ${(item.name)!}" onchange="displayGrade(this.form)"
           style="width:150px;" items=semesters  empty="所有学期"/]
[/@]
[#--
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
[@b.div style="margin-top:10px;text-align:center"]
		${semester.schoolYear}/${semester.name}
        ${b.text("attr.stdNo")}:${(std.code)!}
        ${b.text("attr.personName")}:[@i18nName std/]
        ${b.text("entity.department")}:[@i18nName std.department?if_exists/]
        ${b.text("entity.major")}:[@i18nName std.major?if_exists/]
        ${b.text("entity.direction")}:[#if std.direction?exists][@i18nName std.direction/][#else]无[/#if]
[/@]
--]
[#include "studentGrades.ftl"]
[@b.foot/]
