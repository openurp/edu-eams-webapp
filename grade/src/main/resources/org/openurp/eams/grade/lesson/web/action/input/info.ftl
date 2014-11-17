[#ftl]
[@b.head/]

[@b.toolbar title='教学班成绩 课程序号:${lesson.no} 课程名称:${lesson.course.name}']
	bar.addBack("${b.text("action.back")}");
[/@]
[@b.tabs]
	[@b.tab label="成绩状态"][#include "../components/gradeState.ftl"/][/@]
	[@b.tab label="成绩列表(共${grades?size}人)"][#include "../components/teachClassGrades.ftl"/]	[/@]
[/@]
[@b.form name="actionForm" target="contentDiv"]
    <input type="hidden" name="lessonId" value="${Parameters['lessonId']}"/>
    
[/@]
<script>
     function editGradeState(){
	 	var form = document.actionForm;
     	bg.form.submit(form,"${b.url('!editGradeState')}");
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
     
     [#if orderModify??]
     	jQuery(document).ready(function(){
     		jQuery(".ui-state-default").each(function(){
     			if(jQuery(this).hasClass("ui-state-active")){
     				jQuery(this).removeClass("ui-tabs-selected").removeClass("ui-state-active");	
     			}else{
     				jQuery(this).addClass("ui-tabs-selected").addClass("ui-state-active");
     			}
     		})
     		jQuery(".ui-tabs-panel").each(function(){
     			if(jQuery(this).hasClass("ui-tabs-hide")){
     				jQuery(this).removeClass("ui-tabs-hide");	
     			}else{
     				jQuery(this).addClass("ui-tabs-hide")
     			}
     		})
     	})
     [/#if]
 </script>
[@b.foot/]
