[#ftl/]
[@b.grid items=lessons var="lesson"]
	[@b.row]
		[@b.col width="5%" title="序号"]${lesson_index+1}[/@]
		[@b.col property="no" title="attr.taskNo" width="10%"][@b.a href="/courseTable!taskTable?lesson.id=${lesson.id}" title="查看课程安排"]${lesson.no!}[/@][/@]
		[@b.col property="course.code" title="attr.courseNo" width="10%"/]
		[@b.col property="course.name" title="attr.courseName" width="15%"/]
		[@b.col property="teachClass.name" title="entity.teachClass" width="25%"]${(lesson.teachClass.name)?if_exists?html}[/@]
		[@b.col title="entity.teacher" width="10%"][#list lesson.teachers as teacher][#if teacher_index!=0],[/#if]${teacher.name}[/#list][/@]
		[@b.col title="剩余容量" width="7%" ]${lesson.teachClass.limitCount-lesson.teachClass.stdCount}[/@]
		[@b.col property="teachClass.stdCount" title="实际人数" width="7%"/]
		[@b.col property="teachClass.limitCount" title="上限" width="7%"/]
	[/@]
[/@]
