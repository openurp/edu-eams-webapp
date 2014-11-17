[#ftl]
[@b.head/]
[@b.toolbar title="课程成绩管理"]
		bar.addItem("成绩导入", "importData()");
		[#--]bar.addItem("下载模板", "downloadTemplate()","${base}/static/images/action/download.gif");[--]
	    bar.addItem("不及格成绩", "noPassCourseGrades()");
	    bar.addItem("无成绩学生名单", "noGradeTakes()");
	    
	    
	    function noGradeTakes() {
		    var form = document.gradeIndexForm;
		    form.target="_blank";
	       bg.form.submit(form,"${b.url('college!noGradeTakeList')}");
	       form.target="contentDiv";
	       form.action="courseGrade!search.action";
	    }
	    
	    function noPassCourseGrades() {
		    var form = document.gradeIndexForm;
		    form.target="_blank";
			bg.form.submit(form,"${b.url('college!noPassCourseGradeList')}");
	       	form.target="contentDiv";
	       	form.action="courseGrade!search.action";
	    }
	   
	    
		function downloadTemplate() {
	    	var form = document.gradeIndexForm;
			bg.form.submit('gradeIndexForm', "${b.url('manage!downloadTemplate')}?template=template/excel/GradeTemplate.xls",null,null,false);
		}
	    
	    function importData() {
	    	var form = document.gradeIndexForm;
       		bg.form.addInput(form,"file","template/excel/GradeTemplate.xls");
            bg.form.addInput(form,"display","成绩导入模板");
            bg.form.addInput(form, "importTitle", "学生成绩上传");
            bg.form.submit(form,"${b.url('manage!importForm')}");
	    }
[/@]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
	<table class="indexpanel">
		<tr>
			<td class="index_view">
			[@b.form name="gradeIndexForm" action="!search" title="ui.searchForm" target="contentDiv" theme="search"]
				<input type="hidden" name="lesson.semester.id" value="${semester.id}"/>
				[#include "searchForm.ftl"/]
			[/@]
		   	</td>
			<td class="index_content">
				[@b.div id="contentDiv" href="#" /]
			</td>
		</tr>
	</table>
<script>
	jQuery(function() {
		bg.form.submit(document.gradeIndexForm);
	});
</script>
[@b.foot/]
