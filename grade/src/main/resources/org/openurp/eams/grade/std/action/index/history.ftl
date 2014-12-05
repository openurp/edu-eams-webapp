[#ftl]
[@b.head/]
[@b.toolbar title="grade.allSemesterGrade"]
	bar.addBack("${b.text("action.back")}");
[/@]
	[#include "stdGradeStat.ftl"/]
	[@b.div style="margin-top:10px;text-align:center;font-weight:bold;"]${b.text("grade.resultsList")}[/@]
	[#include "../../../components/studentGrades.ftl"]
[@b.foot/]