[#ftl]
[@b.head/]

[@b.toolbar title='${lesson.course.name}[${lesson.no}]']
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
	[/#if]
	bar.addItem("修改成绩状态","editGradeState()");
	bar.addBack("${b.text("action.back")}");
[/@]
[@b.tabs]
	[@b.tab label="成绩状态"][#include "/com/ekingstar/eams/teach/grade/page/manager/common/gradeState.ftl"/][/@]
	[@b.tab label="成绩列表(共${grades?size}人)"][#include "/com/ekingstar/eams/teach/grade/page/manager/common/teachClassGrades.ftl"/][/@]
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
 </script>
[@b.foot/]
