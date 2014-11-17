[#ftl]
[@b.head/]
	[@b.grid items=teachQualities  var="teachQuality"]
		[@b.gridbar]
			[#if (Parameters['state']!)=='1']
			bar.addItem('打印分析表', 'btnTeachQuilty()');
			bar.addItem('驳回修改', 'editTeachQuilty()');
			[/#if]
		[/@]
		[@b.row]
			[@b.boxcol /]
			[@b.col width="10%" property="lesson.no" title="序号" /]
			[@b.col width="10%" property="lesson.course.code" title="attr.courseNo" maxlength="3"/]
			[@b.col width="15%" property="lesson.course.name" title="attr.courseName" /]
			[@b.col width="15%" property="lesson.courseType.name" title="entity.courseType" /]
			[@b.col width="20%" property="lesson.teachClass.name" title="entity.teachClass"]${teachQuality.lesson.teachClass.name?html!}<br>[/@]
			[@b.col width="10%" property="teacher.name" title="任课教师"]
			[#list (teachQuality.lesson.teachers)! as teacher]
				<input type="hidden" name="teacher.id" value="${(teacher.id)!}" />
				${teacher.name?html!}<br>
			[/#list]
			[/@]
			[@b.col width="10%" property="teacher.name" title="分析人"/]
			[@b.col width="10%" property="flow" title="流程"]
			[#if (teachQuality.flow!0)?string=="0"]系统流程[#else]
			<font color="red">纸质流程</font>[/#if]
			[/@]
		[/@]
[/@]
[@b.form name="taskListForm" id="taskListForm" action="!search" ]
	<input type="hidden" name="semester.id" value="${Parameters['semester.id']}"/>
	<input type="hidden" name="params" value="&semester.id=${Parameters['semester.id']}"/>
[/@]
<script type="text/javascript">
	function btnTeachQuilty() {
		var form = document.taskListForm;
		form.target="_blank";
		[#include "../components/segScore.ftl"/]
		[@addSeqToForm 'form'/]
		bg.form.submitId(form,"teachQuality.id",false,"${b.url('teach-quality-sum!btnTeachQuilty')}");
	}
	
	function editTeachQuilty() {
		var form = document.taskListForm;
		[#include "../components/segScore.ftl"/]
		[@addSeqToForm 'form'/]
        form.target="contentDiv";
        var teachQualityIds = bg.input.getCheckBoxValues("teachQuality.id");
        if(teachQualityIds == null || teachQualityIds ==""){
        	alert("请至少选择一条操作");
        	return;
        }
        bg.form.addInput(form,"teachQualityIds",teachQualityIds);
		bg.form.submit(form,"${b.url('teach-quality-sum!editTeachQuilty')}");
	}
</script>
[@b.foot /]
