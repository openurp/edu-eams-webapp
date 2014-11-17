[#ftl/]
[@b.div id="allSemesterScoreForm"]
	[@b.head/]
	[#include "/template/macros.ftl"/]
	[@b.toolbar title="grade.stdPersonScoreSearch"]
		[#if hasMinor?? && hasMinor]
			bar.addItem("我的一专成绩", "myGrade('MAJOR')");
			bar.addItem("我的二专成绩", "myGrade('MINOR')");
			function myGrade(projectType) {
				bg.Go('${b.url('person!index?projectType=')}' + projectType, 'main');
			}
			bar.addItem("一专所有学期成绩", "historyCourseGrade(document.allSemesterForm, 'MAJOR')");
			bar.addItem("二专所有学期成绩", "historyCourseGrade(document.allSemesterForm, 'MINOR')");
		[#else]
			bar.addItem("所有学期成绩", "historyCourseGrade(document.allSemesterForm, 'MAJOR')");
		[/#if]

		function historyCourseGrade(form, projectType) {
			jQuery('#semesterForm').parent().hide();
			bg.form.submit(form,"${b.url('person!historyCourseGrade?projectType=')}" + projectType);
		}
		
		function personGrade(form){
			form.target="_blank";
			bg.form.submit(form,"${base}/teach/grade/transcript/report.action");
			form.target="allSemesterScoreForm";
		}
	[/@]
    [@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	[#--
    [@b.div style="margin-top:10px;text-align:center"]
            ${b.text("attr.stdNo")}:${(std.code)!}
            ${b.text("attr.personName")}:[@i18nName std/]
            ${b.text("entity.department")}:[@i18nName std.department?if_exists/]
            ${b.text("entity.major")}:[@i18nName std.major?if_exists/]
            ${b.text("entity.direction")}:[#if std.direction?exists][@i18nName std.direction/][#else]无[/#if]
    [/@]
    --]
	[@b.div id="semesterGrade" href="!search?semesterId=${semester.id!}&projectType=${Parameters['projectType']!}"/]

	[@b.form name="allSemesterForm" target="semesterGrade"/]
	[@b.foot/]
[/@]