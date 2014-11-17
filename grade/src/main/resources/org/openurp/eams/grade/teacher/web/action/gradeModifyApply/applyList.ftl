[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.form name="gradeModifyApplySearchForm" action="" target=""]
	[@b.grid items=applys var="apply" filterable="true"]
		[@b.gridfilter property="gradeType.name"]
			<select name="apply.gradeType.id" style="width:95%" onchange="bg.form.submit(this.form)">
			<option value="">...</option>
			[#list gradeTypes?sort_by('code') as gradeType]
			[#if gradeType.id != GA_ID && gradeType.id != FINAL_ID]
			<option value="${gradeType.id}" [#if (Parameters['apply.gradeType.id']!"")=="${gradeType.id}"]selected="selected"[/#if]>[@i18nName gradeType/]</option>
			[/#if]
			[/#list]
			</select>
		[/@]
		[@b.gridfilter property="examStatus.name"]
			<select name="apply.examStatus.id" style="width:95%" onchange="bg.form.submit(this.form)">
			<option value="">...</option>
			[#list examStatuses as examStatus]
			<option value="${examStatus.id}" [#if (Parameters['apply.examStatus.id']!"")=="${examStatus.id}"]selected="selected"[/#if]>[@i18nName examStatus/]</option>
			[/#list]
			</select>
		[/@]
		[@b.gridfilter property="examStatusBefore.name"]
			<select name="apply.examStatusBefore.id" style="width:95%" onchange="bg.form.submit(this.form)">
			<option value="">...</option>
			[#list examStatuses as examStatus]
			<option value="${examStatus.id}" [#if (Parameters['apply.examStatusBefore.id']!"")=="${examStatus.id}"]selected="selected"[/#if]>[@i18nName examStatus/]</option>
			[/#list]
			</select>
		[/@]
		[@b.gridfilter property="status"]
			<select name="applyStatus" style="width:95%" onchange="bg.form.submit(this.form)">
			<option value="">...</option>
			[#list statuses as status]
			<option value="${status.name()}" [#if (Parameters['applyStatus']!"")=="${status.name()}"]selected="selected"[/#if]>${status.fullName}</option>
			[/#list]
			</select>
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="std.code" title="attr.stdNo" width="8%"/]
			[@b.col property="std.name" title="attr.personName" width="8%"/]
			[@b.col property="course.code" title="attr.courseNo" width="8%"/]
			[@b.col property="course.name" title="attr.courseName" width="16%"/]
			[@b.col property="gradeType.name" title="grade.scoreType" width="7%"/]
			[@b.col property="origScoreText" title="field.exam.exam" width="7%"/]
			[@b.col property="scoreText" title="修改后成绩" width="7%"/]
			[@b.col property="examStatusBefore.name" title="exam.situation" width="7%"/]
			[@b.col property="examStatus.name" title="修改后考试情况" width="10%"/]
			[@b.col property="status" title="attr.graduate.auditStatus" width="8%"]${apply.status.fullName}[/@]
			[@b.col property="createdAt" title="attr.graduate.degreeApplication.degreeApplyTime" width="14%"]${apply.createdAt?string('yyyy-MM-dd HH:mm')}[/@]
		[/@]
	[/@]
[/@]
[@b.foot/]
