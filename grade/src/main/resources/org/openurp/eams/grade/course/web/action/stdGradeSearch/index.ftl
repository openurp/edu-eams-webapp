[#ftl]
[@b.head/]
[#include "nav.ftl"/]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="学生成绩查询"]
	bar.addItem("不及格成绩学生名单","notPassed(document.stdGradeSearchForm)");
	
	function notPassed(form){
       	$("#isPass").val(3);
       	bg.form.submit(form);
    }
    
[/@]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="stdGradeSearchForm" action="!search?orderBy=courseGrade.semester.beginOn desc" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="courseGrade.semester.id" value="${semester.id!}"/>
				[@b.textfields names="courseGrade.std.code;attr.stdNo,courseGrade.std.name;attr.personName,courseGrade.std.grade;年级,courseGrade.lessonNo;attr.taskNo,courseGrade.course.code;attr.courseNo,courseGrade.course.name;attr.courseName"/]
				[@b.select  label="entity.courseType" name="courseGrade.courseType.id" items=courseTypes?sort_by(["name"]) empty="..."/]
				[@b.select label="修读类别" items=courseTakeTypes?sort_by("code") empty="..."  name="courseGrade.courseTakeType.id" /]
				[@majorSelect id="s1" projectId="courseGrade.std.project.id" educationId="courseGrade.std.education.id" departId="courseGrade.std.department.id" majorId="courseGrade.std.major.id" directionId="courseGrade.std.direction.id" stdTypeId="courseGrade.std.type.id"/]
				[@b.select  label="状态" name="courseGrade.status" items={'0':'未提交}','1':'已提交未发布','2','已发布'} empty="全部" /]
				[@b.field label="分数范围"]
         			<input name="scoreFrom" value="${Parameters["scoreFrom"]?if_exists}" maxlength="3" style="width:24%"/>-<input name="scoreTo" maxlength="3" value="${Parameters["scoreTo"]?if_exists}" style="width:24%"/>
				[/@]
				[@b.select  label="是否通过" name="isPass" id="isPass" items={'1':'通过','0':'未通过','3','一直未通过'} empty="全部" /]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search?courseGrade.semester.id=${semester.id!}" /]
			</td>
		</tr>
	</table>
[@b.foot/]
