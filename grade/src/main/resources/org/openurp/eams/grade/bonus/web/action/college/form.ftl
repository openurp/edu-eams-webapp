[#ftl]
[@b.head/]
[@b.form name="bonusEditForm" target="courseTakeDiv" action="!searchCourseTakes"]
	<table width="100%" class="infoTable">
        <tr>
            <td width="15%" class="title">${b.text("attr.stdNo")}:</td>
            <td width="35%" class="content"><input type="text" value="" name="stdCode"/><input type="button" value="查询" onClick="bg.form.submit('bonusEditForm')"/></td>
            <td width="15%" class="title">${b.text("attr.personName")}:</td>
            <td width="25%" class="content" id="personName"></td>
        </tr>
        <tr>
            <td class="title">${b.text("entity.major")}:</td>
            <td class="content" id="major"></td>
            <td class="title">${b.text("common.college")}:</td>
            <td class="content" id="department"></td>
        </tr>
        <tr>
            <td class="title">${b.text("std.grade")}:</td>
            <td class="content" id="grade"></td>
            <td class="title">${b.text("entity.studentType")}:</td>
            <td class="content" id="stdType"></td>
        </tr>
    </table>
[/@]
[@b.div id="courseTakeDiv"]
	[@b.grid items=courseTakes?sort_by(["lesson","no"]) var="courseTake" sortable="false"]
		[@b.gridbar]
		[/@]
		[@b.row]
			[@b.col property="lesson.no" title="attr.taskNo"/]
			[@b.col property="lesson.course.code" title="attr.courseNo" /]
			[@b.col property="lesson.course.name" title="attr.courseName"/]
			[@b.col title="最终成绩"]${(lessonGrades.get(courseTake.lesson).scoreText)!}[/@]
			[@b.col title="加分成绩"]<input type="hidden" value="${courseTake.lesson.id}" name="lesson.id"/><input type="text" value="${(lessonGrades.get(courseTake.lesson).getExamGrade(bonus).score)!}" name="bonus_score_${courseTake.lesson.id}" onBlur="validate(this)"/>[/@]
		[/@]
	[/@]
[/@]
[@b.foot/]