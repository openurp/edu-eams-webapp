[#ftl]
[#include "/template/macros.ftl"/]
[#assign gradeTypes=gradeTypes?sort_by('code')/]
[#assign publishTypeMap={}]
[#list grades as grade]
	[#list gradeTypes as gradeType]
		[#assign examGrade=grade.getExamGrade(gradeType)!"null"]
		[#if examGrade!="null" && examGrade.published && !publishTypeMap?keys?seq_contains(gradeType.id)]
			[#assign publishTypeMap=publishTypeMap + {gradeType.id:gradeType} /]
		[/#if]
	[/#list]
[/#list]
[#assign publishTypes=publishTypeMap?values?sort_by('code')/]
[#assign width=36/(publishTypes?size+3)]
[@b.grid items=grades var="grade" filterable="false"]
	[@b.row]
		[@b.col width="10%" title="attr.yearTerm"]${grade.semester.schoolYear!} ${grade.semester.name!}[/@]
		[@b.col width="10%" title="attr.courseNo"]${(grade.course.code)!}[/@]
		[@b.col width="10%" title="attr.taskNo"]${(grade.lessonNo)!}[/@]
		[@b.col width="20%" title="attr.courseName"][@i18nName grade.course/][#if grade.courseTakeType?? && grade.courseTakeType.id !=1]<span style="color:red;">(${grade.courseTakeType.name})</span>[/#if][/@]
		[@b.col width="14%" title="entity.courseType"][@i18nName grade.courseType/][/@]
		[@b.col width="${width}%" title="attr.credit"]${(grade.course.credits)!}[/@]
		[#if grade?exists]
		[#list publishTypes as gradeType]
		[#assign examStyle][#if grade.published]${grade.passed?string("","color:red")}[/#if][/#assign]
		[@b.col width="${width}%" title="${gradeType.name}" style="${examStyle!}"]
	  		[#assign examGrade=grade.getExamGrade(gradeType)!"null"]
	  		[#if examGrade!="null" && examGrade.published]
	  			${examGrade.scoreText!'--'} [#if (examGrade.examStatus.id)?default(1)!=1](${(examGrade.examStatus.name)!})[/#if]
	  		[/#if]
		[/@]
		[/#list]
		[#assign style][#if grade.published]${grade.passed?string("","color:red")}[/#if][/#assign]
		[@b.col width="${width}%" title="最终"  style="${style!}"]
			[#if grade.published]${grade.scoreText!"--"}[#else]未发布[/#if]
		[/@]
		[@b.col width="${width}%" title="attr.gradePoint"]
			[#if grade.published]${grade.gp!}[#else]未发布[/#if]
		[/@]
		[/#if]
		
	[/@]
[/@]