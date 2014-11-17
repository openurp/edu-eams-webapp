[#ftl]
[@b.head/]
	[@b.form name="teachClassGradeReportForm" target="contentDiv" action="!search"]
		[@b.grid items=lessons var="lesson" filterable="true"]
			[@b.row]
				[@b.boxcol width="3%"/]
				[@b.col property="no" title="attr.taskNo" width="10%"][@b.a href="/courseTable!taskTable?lesson.id=${lesson.id}" title="查看课程安排"]${lesson.no!}[/@][/@]
				[@b.col property="course.code" title="attr.courseNo" width="10%"/]
				[@b.col property="course.name" title="attr.courseName" width="10%"/]
				[@b.col property="courseType.name" title="课程类别" width="10%"/]
				[@b.col property="teachDepart.name" title="开课院系" width="10%"/]
				
				[@b.col property="teachClass.name" title="entity.teachClass" width="17%"/][#--[#if (lesson.requirement.isGuaPai)?? && lesson.requirement.isGuaPai]挂牌[#else]${(lesson.teachClass.name)?if_exists?html}[/#if][/@][--]
				[@b.col title="entity.teacher" width="10%"][#list lesson.teachers as teacher][#if teacher_index!=0],[/#if]${teacher.name}[/#list][/@]
				[@b.col title="attr.stdNum" width="5%"]
					[@b.a href='/teachTask!printAttendanceCheckList?lessonIds=${lesson.id}' title='查看点名册' target='_blank']${lesson.teachClass.stdCount}[/@]
				[/@]
				[@b.col title="不及格" width="5%"]
					[#if unpassedMap.get(lesson.id)??]
					[@b.a href='!unpassed?lesson.id=${lesson.id}' title='查看不及格名单' target='_blank']${(unpassedMap.get(lesson.id))!}[/@]
					[#else]0
					[/#if]
				[/@]
				[@b.col property="course.credits" title="attr.credit" width="5%"/]
				[@b.col  title="周时" width="5%"]${lesson.courseSchedule.weeks!}[/@]
			[/@]
		[/@]
	[/@]
<script language="JavaScript">
    jQuery(function(){
		bg.form.addHiddens(document.teachClassGradeReportForm, "[@htm.queryStr/]");
	});
</script>
[@b.foot/]