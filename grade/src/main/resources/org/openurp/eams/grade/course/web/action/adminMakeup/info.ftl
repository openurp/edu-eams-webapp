[#ftl]
[@b.head/]
[@b.toolbar title="成绩列表(${grades?size}) 序号:${lesson.no} 课程:${(lesson.course.name)?if_exists}"]
	bar.addPrint();
	bar.addBackOrClose();
[/@]
[#assign gradeStatus={'0':'新增','1':'确认','2':'已发布'}]
[#assign gradeStatusColor={'0':'#FFBB66','1':'#99FF99','2':'white'}]
[#assign gradeTypes=gradeTypes?sort_by("code")/]

<table width="100%" style="border-collapse: collapse;border:solid;border-width:0px;"><tr><td>序号:${lesson.no!} 课程:${lesson.course.name!}</td>[#list gradeStatus?keys as ss]<td style="background-color:${gradeStatusColor[ss]};text-align:center" width="80px">${gradeStatus[ss]}</td>[/#list]</tr></table>

[@b.grid items=grades var="courseGrade"]
	[@b.row]
		[@b.col width="4%" title="序号"]${courseGrade_index+1}[/@]
		[@b.col property="std.code" title="attr.stdNo"/]
		[@b.col property="std.name" title="attr.personName"/]
		[@b.col property="std.adminclass.name" title="班级"/]
		[@b.col property="courseType.name" title="entity.courseType"/]
		[@b.col property="courseTakeType.name" title="修读类别"/]
		[#list gradeTypes as gradeType]
		[#assign examGrade=(courseGrade.getExamGrade(gradeType))!"null"/]
		[#if examGrade!="null"]
		[@b.col title=gradeType.name width="9%" property="gradeType.${gradeType.id}" style="background-color:${gradeStatusColor[examGrade.status?string]}"]
			[#if !examGrade.passed]<font color='red'>${examGrade.scoreText!"--"}[#if examGrade.examStatus.id!=NORMAL.id]<sup>${examGrade.examStatus.name}</sup>[/#if]</font>
			[#else]
			${examGrade.scoreText!"--"}[#if examGrade.examStatus.id!=NORMAL.id]<sup>${examGrade.examStatus.name}</sup>[/#if]
			[/#if]
		[/@]
		[#else][@b.col title=gradeType.name width="9%" property="gradeType.${gradeType.id}"][/@][/#if]
		[/#list]
	[/@]
[/@] 
[@b.foot/]