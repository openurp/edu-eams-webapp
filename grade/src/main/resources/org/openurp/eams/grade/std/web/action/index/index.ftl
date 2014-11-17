[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="grade.stdPersonScoreSearch"]
	   bar.addItem("${b.text('grade.allSemesterGrade')}", "historyCourseGrade(document.allSemesterForm)");
	   function historyCourseGrade(form) {bg.form.submit(form,"${b.url('!history')}");}
	   function personGrade(form){
		   	form.target="_blank";
		   	bg.form.submit(form,"${base}/teach/grade/transcript/report.action");
	   		form.target="allSemesterScoreForm";
	   }
[/@]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
[@b.div style="margin-top:10px;text-align:center"]
		${semester.schoolYear}/${semester.name}
        ${b.text("attr.stdNo")}:${(std.code)!}
        ${b.text("attr.personName")}:[@i18nName std/]
        ${b.text("entity.department")}:[@i18nName std.department?if_exists/]
        ${b.text("entity.major")}:[@i18nName std.major?if_exists/]
        ${b.text("entity.direction")}:[#if std.direction?exists][@i18nName std.direction/][#else]无[/#if]
[/@]
[#include "studentGrades.ftl"]
[@b.form name="allSemesterForm"/]
[@b.foot/]
