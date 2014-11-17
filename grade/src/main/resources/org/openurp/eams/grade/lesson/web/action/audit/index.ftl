[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>
[@b.toolbar title="开课院系课程成绩管理"]
	bar.addItem("不及格成绩", "noPassCourseGrades()");
	bar.addItem("无成绩学生名单", "noGradeTakes()");
	[#--bar.addItem("成绩状态统计","scoreStatusStat()");--]
	
    function noGradeTakes(){
    	var form = document.gradeIndexForm;
        bg.form.submit(form,"${b.url('!noGradeTakes')}","_blank");
    }
    function noPassCourseGrades(){
   		var form = document.gradeIndexForm;
        bg.form.submit(form,"${b.url('!unPassedGrades')}","_blank");
    }
    
    
    function scoreStatusStat(){
    	var form = document.gradeIndexForm;
    	form.target="_blank";
    	bg.form.submit(form,"${b.url('state!index')}")
        form.action="${b.url('!search')}";
        form.target="contentDiv";
    }
[/@]
	[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view"  style="width:180px">
			[@b.form name="gradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="lesson.semester.id" value="${semester.id}"/>
			[#assign statusParam = 2/]
			[#assign extraSearchTR]
				[@b.select label="成绩类型" items=gradeTypes name="statusGradeTypeId"/]
			    [#if setting.submitIsPublish]
			    [@b.radios label="" name="status" items="0:未提交,2:已发布" value="2"/]
			    [#else]
			    [@b.radios label="" name="status" items="0:未提交,1:已提交未发布,2:已发布" value="1"/]
			    [#assign statusParam = 1/]
			    [/#if]
			[/#assign]
			[#include "../components/taskBasicForm.ftl"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="!search?lesson.semester.id=${(semester.id)?default('')}&statusGradeTypeId=${(gradeTypes?first.id)!}&status=${statusParam}" /]
			</td>
		</tr>
	</table>
[@b.foot/]
