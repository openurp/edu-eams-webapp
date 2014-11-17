[#ftl]
[#include "/template/macros.ftl"/]
[@b.head/]
	[@b.toolbar title="成绩添加结果"]
		bar.addItem("继续添加","continueAddGrade()");
		bar.addBack();
		
		function continueAddGrade(){
			var form = document.stdGradeForm;
			bg.form.submit(form,"${b.url('std-grade!batchAdd')}","contentDiv",null,null,null);
			form.action="${b.url('std-grade!search')}";
		}
	[/@]
	[@b.div style="text-align:center;"]${semester.schoolYear} ${semester.name} [@i18nName gradeType/]添加结果[/@]
	[@b.grid items=grades var="grade"]
		[@b.row]
			[@b.boxcol/]
			[@b.col title="attr.stdNo"]${grade.std.code}[/@]
			[@b.col title="attr.personName"][@i18nName grade.std/][/@]
			[@b.col title="attr.taskNo"]${grade.lessonNo?if_exists}[/@]
			[@b.col title="attr.courseNo"]${grade.course.code}[/@]
			[@b.col title="entity.course"][@i18nName grade.course?if_exists/][/@]
			[@b.col title="entity.courseType"][@i18nName grade.courseType?if_exists/][/@]
			[@b.col title="成绩"]${(grade.getExamGrade(gradeType).scoreText)!}[/@]
		[/@]
	[/@]
[@b.foot/]
