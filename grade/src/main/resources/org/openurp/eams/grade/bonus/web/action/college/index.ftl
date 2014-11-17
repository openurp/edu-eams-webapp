[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>
[@b.toolbar title="加分成绩管理"/]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view"  style="width:180px">
			[@b.form name="bonusIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				[@b.textfields  names="examGrade.courseGrade.std.code;attr.stdNo,examGrade.courseGrade.std.name;attr.personName,examGrade.courseGrade.lesson.no;attr.taskNo,examGrade.courseGrade.lesson.course.code;attr.courseNo,examGrade.courseGrade.lesson.course.name;attr.courseName"/]
				[@b.select  label="学生类别" name="examGrade.courseGrade.std.type.id" items=(stdTypes)?sort_by("code") empty="${b.text('common.all')}"/]
				[@b.select  label="entity.courseType" name="examGrade.courseGrade.lesson.courseType.id" items=courseTypes?sort_by(["name"]) empty="${b.text('common.all')}"/]
				[@b.select  label="attr.teachDepart" name="examGrade.courseGrade.lesson.teachDepart.id" items=(departments)?sort_by("code") empty="${b.text('common.all')}"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search" /]
			</td>
		</tr>
	</table>
[@b.foot/]
