[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
<script src="${base}/static/scripts/StringUtils.js"></script>
<script language="JavaScript" type="text/JavaScript" src='${base}/dwr/engine.js'></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/dwr/interface/gradeCalcualtor.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/grade/course/onreturn.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/grade/course/input.js?ver=201206051"></script>
[#assign gradeStatus={'0':'新添加','1':'已提交','2':'已发布'}]
[#assign gradeStatusColor={'0':'#FFBB66','1':'#99FF99','2':'white'}]
    [@b.toolbar title="grade.teacher.modify.apply"]
    	bar.addItem("${b.text('action.application')}","save()");
        bar.addBack("${b.text("action.back")}");
        
        function save(){
        	[#list examGrades?sort_by(['gradeType','code']) as examGrade]
        	if((jQuery("input[name='hidden_grade_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}']").val() != jQuery("#${(examGrade.gradeType.shortName)!}_1").val()) || (jQuery("input[name='hidden_status_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}']") != null && jQuery("input[name='hidden_status_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}']").val()!= jQuery("#examStatus_${(examGrade.gradeType.shortName)!}_${(examGrade_index + 1)}").val())){
        		if(jQuery("input[name='applyReason_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}']").val() == "" || jQuery("input[name='applyReason_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}']").val() == null){
    				alert("${examGrade.gradeType.name}已有变更,请填写申请理由");    		
	        		return;
        		}
        	}
        	[/#list]
        	bg.form.submit(document.gradeModifyApplyEditForm);
        }
        
        function tempCheckScore(input){
	    	if(input.value){
	    		if(isNaN(input.value)){
	    			input.value = "";
	    		}else if (input.value > 100){
	    			input.value = 100;
	    		}else if (input.value < 0){
	    			input.value = 0;
	    		}
	    	}
    }
    [/@]
    [@b.form action="!save" title="grade.teacher.modify.stdGrade" name="gradeModifyApplyEditForm"]
 		<table>
 			<tr>
 				<td>
 					${b.text("attr.stdNo")}:
 				</td>
 				<td>
 					<input type="hidden" value="${(courseGrade.id)!}" name="courseGrade.id"/>
		    	 	<input type="hidden" id="courseTakeType_1" value="${courseGrade.courseTakeType.id}"/>
		    	 	<input type="hidden" value="${(courseGrade.project.id)?if_exists}" id="courseTake_project_1">
 					${courseGrade.std.code}
 				</td>
 				<td>
 					${b.text("std.name")}:
 				</td>
 				<td>
 					[@i18nName courseGrade.std/]
 				</td>
 				<td>
 					${b.text("grade.teacher.modify.passed")}:
 				</td>
 				<td>
 					${(courseGrade.passed)?if_exists?string("是","<font color='red'>否</font>")}
 				</td>
 				<td>
 					${b.text("attr.status")}:
 				</td>
 				<td>
 					${gradeStatus[courseGrade.status?default(0)?string]!}
 				</td>
 			</tr>
 		</table>
 		<script language="JavaScript">
			[#assign inputGradeTypes=[]]
		    [#list courseGrade.examGrades as examGrade]
		    	[#assign g = examGrade.gradeType/]
		    	[#if g.id=GA_ID]
		    	[#assign hasGa=true/]
		    	[#assign inputGradeTypes=inputGradeTypes + [g]]
		    	[#else]
		    	[#assign inputGradeTypes=inputGradeTypes + [g]]
		    	[/#if]
		    [/#list]
		    gradeTable = new GradeTable();
		    [#list inputGradeTypes?sort_by('code') as gradeType]
		    gradeTable.gradeState[${gradeType_index}] = new Object();
		    gradeTable.gradeState[${gradeType_index}].id = "${(gradeType.id)!}";
		    gradeTable.gradeState[${gradeType_index}].name = "${(gradeType.shortName)!}";
		    gradeTable.gradeState[${gradeType_index}].percent = ${(gradeState.getPercent(gradeType))?default("null")};
		    gradeTable.gradeState[${gradeType_index}].inputable=true;
		    [/#list]
		    gradeTable.precision=${gradeState.precision};
		    gradeTable.gradeStateId=${gradeState.id};
		    gradeTable.hasGa=${(hasGa!false)?string("true","false")};
		</script>
 		[@b.grid items=examGrades?sort_by(['gradeType','code']) var="examGrade"]
 			[@b.row]
 				[@b.col width="20%" property="gradeType.name" title="grade.scoreType"/]
				[@b.col width="10%" property="score" title="field.exam.exam"]
					<input type="hidden" name="examGrade.id" value="${examGrade.id}"/>
					[#if examGrade.passed]${(examGrade.scoreText)?if_exists}[#else]<font color="red">${(examGrade.scoreText)?if_exists}</font>[/#if]
					<input type="hidden" name="hidden_grade_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}" value="${(examGrade.score)?if_exists}"/>
				[/@]
				[@b.col width="10%" title="新成绩"]
			    [#if examGrade.markStyle.numStyle]
				        <input type="text" class="text" onfocus="this.style.backgroundColor='yellow'" onblur="tempCheckScore(this);this.style.backgroundColor='white';" id="${(examGrade.gradeType.shortName)!}_1" name="${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}" value="${(examGrade.score)!}" style="width:75px" maxlength="4"/>
				    [#else]
				        <select onfocus="this.style.backgroundColor='yellow'" onblur="this.style.backgroundColor='white'" style="width:80px" id="${(examGrade.gradeType.shortName)!}_1" name="${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}" style="width:80px">
				            <option value="">...</option>
				            [#list (gradeConverterConfig.items)! as item]
				            <option value="${item.defaultScore}" [#if (examGrade.score)?? && examGrade.score == item.defaultScore ]selected[/#if]>${item.grade}</option>
				            [/#list]
						</select>
				    [/#if]
				[/@]
				[@b.col width="15%" title="exam.situation"]
				    [#if tempGa || (examGrade.gradeType.examType)??]
				    	[@b.select items=examStatuses value=((examGrade.examStatus)!normalExamStatus) name="examStatus_" + (examGrade.gradeType.shortName)! + "_" + courseGrade.std.id id="examStatus_" + (examGrade.gradeType.shortName)! + "_" + (examGrade_index + 1) style="width:50px;" onchange="checkScore(1, this)"/]
				    	<input type="hidden" name="hidden_status_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}" value="${(examGrade.examStatus.id)?if_exists}"/>
				    [/#if]
				[/@]
				[@b.col width="15%" title="grade.percent"]
					[#if (examGrade.percent)??]${examGrade.percent}%[#elseif (gradeState.getPercent(examGrade.gradeType)?string.percent)??]${gradeState.getPercent(examGrade.gradeType)?string.percent}[/#if]
				[/@]
				[@b.col width="30%" title="申请理由"]
					<input style="width:95%" type="text" maxLength="100" name="applyReason_${(examGrade.gradeType.shortName)!}_${courseGrade.std.id}" />
				[/@]
 			[/@]
 		[/@]
    [/@]
[@b.foot/]