[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#assign gradeStatus={'0':'新添加','1':'已提交','2':'已发布'}]
[@b.form name="gradeModifyApplySearchForm" action="" target=""]
	[@b.grid items=courseGrades var="courseGrade" filterable="true"]
		[@b.gridbar]
			bar.addItem("${b.text('grade.teacher.modify.apply')}",action.edit());
		[/@]
		[@b.gridfilter property="courseType.name"]
			<select name="courseGrade.courseType.id" style="width:95%" onchange="bg.form.submit(this.form)">
			<option value="">...</option>
			[#list courseTypes as courseType]
			<option value="${courseType.id}" [#if (Parameters['courseGrade.courseType.id']!"")=="${courseType.id}"]selected="selected"[/#if]>[@i18nName courseType/]</option>
			[/#list]
			</select>
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="std.code" title="attr.stdNo" width="10%"/]
			[@b.col property="std.name" title="attr.personName" width="10%"/]
			[@b.col property="lessonNo" title="attr.taskNo" width="10%"/]
			[@b.col property="course.code" title="attr.courseNo" width="10%"/]
			[@b.col property="course.name" title="entity.course" width="20%"/]
			[@b.col property="courseType.name" title="entity.courseType" width="17%"/]
			[@b.col property="score" title="field.exam.exam" width="6%"]
				[#if courseGrade.passed]${(courseGrade.scoreText)?if_exists}[#else]<font color="red">${(courseGrade.scoreText)?if_exists}</font>[/#if]
			[/@]
			[@b.col property="course.credits" title="attr.credit" width="5%"/]
			[@b.col property="gp" title="attr.gradePoint" width="5%"][#if courseGrade.passed]${(courseGrade.gp?string("#.##"))?if_exists}[#else]<font color="red">${(courseGrade.gp?string("#.##"))?if_exists}</font>[/#if][/@]
		[/@]
	[/@]
[/@]
[@b.foot/]
