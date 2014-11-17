[#ftl]
[@b.head/]
[@b.toolbar title="课程成绩管理"]
		bar.addItem("成绩导入", "importData()");
		bar.addItem("下载模板", "downloadTemplate()","${base}/static/images/action/download.gif");
		function downloadTemplate() {
	    	var form = document.gradeIndexForm;
			bg.form.submit('gradeIndexForm', "${b.url('manage!downloadTemplate')}?template=template/excel/GradeTemplate.xls",null,null,false);
		}
	    
	    function importData() {
	    	var form = document.gradeIndexForm;
       		bg.form.addInput(form,"file","template/excel/GradeTemplate.xls");
            bg.form.addInput(form,"display","成绩导入模板");
            bg.form.addInput(form, "importTitle", "学生成绩上传");
            bg.form.submit(form,"${b.url('!importForm')}", "_blank");
	    }
[/@]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view" style="width:180px">
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
				[@b.div id="contentDiv" href="!search?lesson.semester.id=${(semester.id)?default('')}&statusGradeTypeId=${(gradeTypes?first.id)!}&status=${statusParam}"  /]
			</td>
		</tr>
	</table>
[@b.foot/]
