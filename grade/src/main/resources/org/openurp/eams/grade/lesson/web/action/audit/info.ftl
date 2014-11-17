[#ftl]
[@b.head/]

[@b.toolbar title='教学班成绩 课程序号:${lesson.no} 课程名称:${lesson.course.name}']	
	[#if gradeState??]
		[#if gradeState.getState(gradeType)?? && ((gradeState.getState(gradeType).status!0))==1]
			bar.addItem("发布${gradeType.name}", "publishGradeInfo('${gradeType.id}')");
			bar.addItem("退回修改${gradeType.name}","drawback(${gradeType.id})");
		[/#if]
	[/#if]
	bar.addBack("${b.text("action.back")}");
[/@]
[@b.tabs]
	[@b.tab label="成绩状态"][#include "../components/gradeState.ftl"/][/@]
	[@b.tab label="成绩列表(共${grades?size}人)"][#include "../components/teachClassGrades.ftl"/]	[/@]
[/@]
[@b.form name="actionForm" target="contentDiv"]
    <input type="hidden" name="lessonId" value="${Parameters['lessonId']}"/>
    [#if gradeState??]
    <input type="hidden" name="gradeState.id" value="${gradeState.id}"/>
    [/#if]
    
[/@]
<script>
	function publishGradeInfo(gradeTypeId) {
		var form = document.actionForm;
		var lessonIds = "${lesson.id}";
	    if (confirm("确定要发布当前指定的成绩吗？")) {
	        bg.form.addInput(form, "isPublished", 1, "hidden");
	        if (null != gradeTypeId) {
	            bg.form.addInput(form, "gradeTypeId", gradeTypeId, "hidden");
	        }
	        bg.form.addInput(form, "lessonIds", lessonIds, "hidden");
	        bg.form.addInput(form, "refer", "${action.class.name}", "hidden");
        	bg.form.submit(form,"${b.url('publish!publish')}");
	    }
	}
	
     function editGradeState(){
	 	var form = document.actionForm;
     	bg.form.submit(form,"${b.url('!editGradeState')}");
     }
     function drawback(gradeTypeId){
	 	var form = document.actionForm;
		var reason = prompt("请填写审核理由(必填且不超过100个字)。")
			if(reason == null || reason =="" || reason.length >100){
				alert("审核理由不符合规格");
				return;
			}
		bg.form.addInput(form,"auditReason",reason);
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
     	if(confirm("确认删除选择的成绩吗?\n说明：删除成绩后，教师还可以再次录入。")){
     		bg.form.addInput(form,"courseGradeIds",gradeIds);
     		bg.form.submit(form,"${b.url('!remove')}");
     	}
     }
 </script>
[@b.foot/]
