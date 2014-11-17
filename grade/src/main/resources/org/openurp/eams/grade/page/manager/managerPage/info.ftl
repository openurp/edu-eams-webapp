[#ftl]
[@b.head/]

[@b.toolbar title='${lesson.course.name}[${lesson.no}]']
	bar.addItem("${b.text('action.edit')}","edit()");
	bar.addItem("${b.text('action.delete')}","remove()");
	[#if gradeState??]
		var drawbackMenu = bar.addMenu("退回");
		[#list gradeTypes?sort_by("code") as gradeType]
			[#if gradeState.getState(gradeType)?? && ((gradeState.getState(gradeType).status!GRADE_STATUS_NEW)) == GRADE_STATUS_SUBMITED]
				drawbackMenu.addItem("${gradeType.name}","drawback(${gradeType.id})");
			[/#if]
		[/#list]
		
		var publishMenu = bar.addMenu("发布");
		[#list publishableGradeTypes?sort_by("code") as gradeType]
			[#if gradeState.getState(gradeType)?? && ((gradeState.getState(gradeType).status!GRADE_STATUS_NEW)) == GRADE_STATUS_SUBMITED]
				publishMenu.addItem("${gradeType.name}", "publishExamGrade('${gradeType.id}')");
			[/#if]
		[/#list]
		
		var revokeMenu = bar.addMenu("取消发布");
		[#list gradeTypes?sort_by("code") as gradeType]
			[#if gradeState.getState(gradeType)?? && ((gradeState.getState(gradeType).status!GRADE_STATUS_NEW)) == GRADE_STATUS_PUBLISHED]
				revokeMenu.addItem("${gradeType.name}", "revokeExamGrade('${gradeType.id}')");
			[/#if]
		[/#list]
		
	[/#if]
	bar.addItem("修改成绩状态","editGradeState()");
	bar.addBack("${b.text("action.back")}");
[/@]
[@b.tabs]
	[@b.tab label="成绩状态"][#include "../common/gradeState.ftl"/][/@]
	[@b.tab label="成绩列表(共${grades?size}人)"][#include "../common/teachClassGrades.ftl"/][/@]
[/@]
[@b.form name="actionForm" target="contentDiv"]
    <input type="hidden" name="lesson.id" value="${lesson.id}"/>
    <input type="hidden" name="back2info" value="1" />
    [#if gradeState??]
    <input type="hidden" name="gradeState.id" value="${gradeState.id}"/>
    [/#if]
[/@]
<script>
	function editGradeState(){
		var form = document.actionForm;
		bg.form.submit(form,"${b.url('!editGradeState')}");
	}
	function publishExamGrade(gradeTypeId) {
		var form = document.actionForm;
	    if (confirm("确定要发布当前指定的成绩吗？")) {
            bg.form.addInput(form, "gradeTypeId", gradeTypeId, "hidden");
        	bg.form.submit(form,"${b.url('!publish')}");
	    }
	}
	function revokeExamGrade(gradeTypeId) {
		var form = document.actionForm;
	    if (confirm("确定要发布当前指定的成绩吗？")) {
            bg.form.addInput(form, "gradeTypeId", gradeTypeId, "hidden");
        	bg.form.submit(form,"${b.url('!revoke')}");
	    }
	}
     function drawback(gradeTypeId){
	 	var form = document.actionForm;
	 	/*
		var reason = prompt("请填写审核理由(必填且不超过100个字)。")
		if(reason == null || reason =="" || reason.length >100){
			alert("审核理由不符合规格");
			return;
		}
		bg.form.addInput(form,"auditReason",reason);
		*/
 		bg.form.addInput(form,"gradeType.id",gradeTypeId);
 		bg.form.submit(form,"${b.url('!drawback')}");
     }
     function edit(){
 	    var form = document.actionForm;
     	var gradeIds = bg.input.getCheckBoxValues("courseGrade.id");
     	if(gradeIds == null || gradeIds == "" || gradeIds.indexOf(",")>-1){
     		alert("请选择一条记录进行操作");
     		return false;
     	}
     	bg.form.addInput(form,"courseGradeId",gradeIds);
     	bg.form.submit(form,"${b.url('!edit')}");
     }
     
     function remove(){
     	var form = document.actionForm;
     	var gradeIds = bg.input.getCheckBoxValues("courseGrade.id");
     	if(gradeIds == null || gradeIds == ""){
     		alert("请选择一条或多条记录");
     		return false;
     	}
     	if(confirm("确认删除选择的成绩吗?\n说明：删除成绩不可恢复,请谨慎操作")){
     		bg.form.addInput(form,"courseGradeIds",gradeIds);
     		bg.form.submit(form,"${b.url('!remove')}");
     	}
     }
 </script>
[@b.foot/]
