[#ftl]
[@b.head/]
<script src="${base}/static/scripts/StringUtils.js"></script>
<script language="JavaScript" type="text/JavaScript" src='${base}/dwr/engine.js'></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/dwr/interface/gradeCalcualtor.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/grade/course/onreturn.js"></script>
<script>
	var emptyScoreStatuses=[[#list setting.emptyScoreStatuses as s]'${s.id}'[#if s_has_next],[/#if][/#list]];
</script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/grade/course/input.js?ver=20120607"></script>
[#include "/template/macros.ftl"/]

[#macro gradeTd(grade, gradeType, courseTake, index)]
<td id="TD_${(gradeType.shortName)!}_${courseTake.std.id}">
[#local examStatus=NormalExamStatus/]
[#--查找考试记录中的考试情况--]
[#if stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")]??]
	[#local examTake=stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")]]
	[#if stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")].examStatus.id != NormalExamStatus.id]
		[#local unNormalExamStatus=stdExamTypeMap[courseTake.std.id + "_" + (gradeType.examType.id)?default("")].examStatus]
		[#local examStatus=unNormalExamStatus/]
	[/#if]
[/#if]
[#--根据策略是否显示输入框--]
[#if gradeTypePolicy.isGradeFor(courseTake,gradeType,examTake)]
	[#--判断不能录入考试分数的情况--]
	[#local couldInput=true/]
	[#if unNormalExamStatus??]
		[#list  setting.emptyScoreStatuses as s][#if s.id==unNormalExamStatus.id][#local couldInput=false/][#break/][/#if][/#list]
	[/#if]
	[#local currentScoreMarkStyle = gradeState.getState(gradeType).scoreMarkStyle/]
	[#if !couldInput]
		<input type="hidden" value="" id="${(gradeType.shortName)!}_${index + 1}" name="${(gradeType.shortName)!}_${courseTake.std.id}"/>
		<input type="hidden" value="${unNormalExamStatus.id}" name="examStatus_${(gradeType.shortName)!}_${courseTake.std.id}" id="examStatus_${(gradeType.shortName)!}_${index + 1}"/>
		${unNormalExamStatus.name}
	[#else]
	[#if (grade.getExamGrade(gradeType))??] [#local examGrade=grade.getExamGrade(gradeType)/][/#if]
	    [#if currentScoreMarkStyle.numStyle]
	        <input type="text" class="text"
	            onfocus="this.style.backgroundColor='yellow'" 
	            onblur="checkScore(${index + 1}, this);this.style.backgroundColor='white';"
	            tabIndex="${index+1}"
	            id="${(gradeType.shortName)!}_${index + 1}" name="${(gradeType.shortName)!}_${courseTake.std.id}"
	    		value="[#if grade?string != "null"]${(examGrade.score)!}[/#if]" style="width:40px" maxlength="4" role="gradeInput"/>
	    [#else]
	            <select onfocus="this.style.backgroundColor='yellow'"
	                onblur="this.style.backgroundColor='white'"
	                onchange="checkScore(${index + 1}, this)"
	                id="${(gradeType.shortName)!}_${index + 1}" name="${(gradeType.shortName)!}_${courseTake.std.id}"
	                style="width:70px" role="gradeInput">
	            <option value="">...</option>
	            [#list gradeRateConfigs.get(currentScoreMarkStyle).items?sort_by('defaultScore')?reverse as item]
		        	<option value="${item.defaultScore}" [#if (examGrade.score)?? && examGrade.score == item.defaultScore ]selected[/#if]>${item.grade}</option>
		        [/#list]
	            </select>
	    [/#if]
	    [#if gradeType.examType??]
	    [@b.select items=examStatuses value=((examGrade.examStatus)!examStatus) name="examStatus_" + (gradeType.shortName)! + "_" + courseTake.std.id id="examStatus_" + (gradeType.shortName)! + "_" + (index + 1) style="width:60px;" 
	    onchange="changeExamStatus('${(gradeType.shortName)!}_${index + 1}',this);checkScore(${index + 1}, this)"/]
	    [/#if]
    [/#if]
[/#if]
</td>
[/#macro]

[#macro displayGrades(index, courseTake)]
    <td align="center">${index + 1}</td>
    <td>${courseTake.std.code}<input type="hidden" value="${(courseTake.std.project.id)?if_exists}" id="courseTake_project_${index + 1}"></td>
	[#if gradeMap.get(courseTake.std)??]
	[#local grade = gradeMap.get(courseTake.std)]
	[/#if]
    <td>
    	[#assign gradeTypeIdSeq =""/]
    	[#list gradeTypes?sort_by('code') as gradeType]
    		 [#if gradeType.id!=GA.id]
	    		 <input type="hidden" id="personPercent_${(gradeType.shortName)!}_${index + 1}" name="personPercent_${(gradeType.shortName)!}_${courseTake.std.id}" value="${(grade.getExamGrade(gradeType).percent)!}"/>
	    		 [#assign gradeTypeIdSeq = gradeTypeIdSeq + gradeType.id/]
	    		 [#if gradeType_has_next]
	    		 	[#assign gradeTypeIdSeq = gradeTypeIdSeq + ","/]
	    		 [/#if]
    		 [/#if]
    	[/#list]
    	[#--<a href="#" onClick="setPersonPercent('${courseTake.std.id}','${gradeTypeIdSeq}','${(grade.id)!}')" title="设置个人百分比">--]${courseTake.std.name}[#--</a>--]
    	[#if courseTake.courseTakeType != NormalTakeType]
    		<sup>${courseTake.courseTakeType.name}</sup>
    	[/#if]
    </td>
    <script language="javascript">courseGrade = gradeTable.add(${index}, "${courseTake.std.id}", ${courseTake.courseTakeType.id});</script>
    <input type="hidden" id="courseTakeType_${index + 1}" value="${courseTake.courseTakeType.id}"/>
    
    [#list gradeTypes?sort_by('code') as gradeType]
    <script>courseGrade.examGrades["${(gradeType.shortName)!}"] = "${(grade.getExamGrade(gradeType).score)!0}";</script>
    [#if gradeType.id==GA.id]
    <td align="center" id="GA_${index + 1}">[#if grade?exists]${grade.getScoreText(GA)!}[/#if]</td>
    [#else]
        [#if (grade.getExamGrade(gradeType).confirmed)!false]
			<td>${grade.getScoreText(gradeType)!}</td>
        [#else]
            [@gradeTd grade, gradeType, courseTake, index/]
        [/#if]
    [/#if]
    
    [/#list]
[/#macro]
[@b.toolbar title="${b.text('grade.teachClassInput')}"]
	bar.addClose();
[/@]
[@b.messages slash="6"/]
<script language="JavaScript">
	[#assign inputGradeTypes=[]]
    [#list gradeTypes as g]
    	[#if g.id=GA.id]
    	[#assign hasGa=true/]
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

    <div align="center" style="font-size:15px;font-weight:bold">[@i18nName lesson.project.school/]课程成绩登记表<br>
${lesson.semester.schoolYear!}学年${(lesson.semester.name)?if_exists?replace('0','第')}学期
    </div>
    [#if courseTakes?size == 0]
   	<br/>
    <table width="90%" align="center" style="background-color:yellow">
        <tr style="color:red">
            <th>当前没有可以录入成绩的学生。<th>
        </tr>
    </table>
    <br/>
    [/#if]
    [@b.form name="gradeForm" action="!save"]
    <input name="lessonId" value="${lesson.id}" type="hidden"/>
    <table align="center" border="0" style="font-size:13px;border-collapse: collapse;border:solid;border-width:1px;border-color:Wheat;width:98%;">
        <tr style="background-color: #FFFFBB">
            <td width="33%">${b.text("attr.courseNo")}:${lesson.course.code}</td>
            <td width="33%">${b.text("attr.courseName")}:${lesson.course.name}</td>
            <td align="left">${b.text("entity.courseType")}:${lesson.courseType.name}</td>
        </tr>
       	<tr style="background-color: #FFFFBB">
            <td>${b.text("attr.taskNo")}:${(lesson.no)?if_exists}</td>
            <td>${b.text("task.courseSchedule.primaryTeacher")}:[@getTeacherNames lesson.teachers/]</td>
            <td>
            	${b.text("grade.recordMode")}:
	            <input type="radio" name="inputTabIndex" onclick="gradeTable.changeTabIndex(this.form,true)" value="1" checked>
	            ${b.text("grade.recordModeByStd")}
	            <input type="radio"  name="inputTabIndex" value="0" onclick="gradeTable.changeTabIndex(this.form,false)" >
	            ${b.text("grade.recordModeByScore")}
            </td>
        </tr>
       	<tr style="background-color: #FFFFBB">
            <td>所录成绩:[#list gradeTypes as gradeType][@i18nName gradeType/]&nbsp;[#if (gradeState.getPercent(gradeType)?string.percent)??](${gradeState.getPercent(gradeType)?string.percent})[/#if][/#list]</td>
            <td>成绩精确度:[#if gradeState.precision=0]${b.text('grade.precision0')}[/#if][#if gradeState.precision=1]${b.text('grade.precision1')}[/#if]</td>
            <td id="timeElapse"></td>
        </tr>
    </table>
    <table class="gridtable" style="width:98%" align="center" onkeypress="gradeTable.onReturn.focus(event)">
        <tr align="center" style="backGround-color:LightBlue">
        [#assign canInputedCount = 0/]
        [#list 1..2 as i]
            <td align="center" width="20px">${b.text("attr.index")}</td>
            <td align="center" width="50px">${b.text("attr.stdNo")}</td>
            <td width="50px">${b.text("attr.personName")}</td>
            [#list gradeTypes?sort_by('code') as gradeType]
                [#if i == 1 && !gradeState.getState(gradeType).confirmed]
                    [#assign canInputedCount = canInputedCount + 1/]
                [/#if]
            	<td  width="[#if gradeType.id=GA.id || !(gradeType.examType)??]40px[#else]90px[/#if]">[@i18nName gradeType/]</td>
            [/#list]
        [/#list]
        </tr>
        [#assign courseTakes = courseTakes?sort_by(["std", "code"])/]
        [#assign pageSize = ((courseTakes?size + 1) / 2)?int/]
        [#list courseTakes as courseTake]
        <tr align="center" style="backGround-color:MintCream">
            [@displayGrades courseTake_index, courseTakes[courseTake_index]/]
            
            [#assign j = courseTake_index + pageSize/]
            [#if courseTakes[j]?exists]
                [@displayGrades j, courseTakes[j]/]
            [#else]
                [#list 1..3 + canInputedCount as i]
            <td></td>
                [/#list]
            [/#if]
            [#if !courseTakes[courseTake_index + 1]?exists || ((courseTake_index + 1) * 2 >= courseTakes?size)]
        </tr>
                [#break]
            [/#if]
        </tr>
        [/#list]
    </table>
    [#if courseTakes?size != 0]
    <table width="100%" style="font-size:15px" height="70px">
        <tr>
            <td align="center" id="submitTd">[#if (canInputedCount > 0 && lesson.teachClass.courseTakes?size > 0)][@b.submit value="暂存" onsubmit="saveGrade(true)" title="${b.text('grade.remaindInputNextTime')}" id="bnJustSave"/] &nbsp;&nbsp;&nbsp;[@b.submit value="提交" onsubmit="saveGrade(false)" id="bnSubmit"/][/#if]</td>
        </tr>
    </table>
    [/#if]
    [/@]
    [@b.form name="goBackForm" target="contentDiv" action="${b.url('!inputTask')}?lessonId=${lesson.id}"/]
<script language="JavaScript">
    gradeTable.changeTabIndex(document.gradeForm,true);
    jQuery(document).ready(function(){
    	jQuery("input[role='gradeInput']").each(function(){
    		var obj = document.getElementById("examStatus_" + this.id);
    		var scoreId = this.id;
			changeExamStatus(scoreId,obj);
    	})
    })
    
    [#if courseTakes?size != 0]
        [#if (canInputedCount > 0)]
        [#else]
    	document.getElementById("timeElapse").innerHTML = "<span style=\"color:red;font-weight:bold;background:yellow;font-size:14pt\">当前成绩录入完成。</span>";
        [/#if]
    [/#if]
    
    function setPersonPercent(stdId,gradeTypeIds,gradeId){
    	jQuery.colorbox({
			transition:"none",
			width:"40%", 
			height:"50%",
			href : "${b.url("!personPercent")}?student.id="
					+stdId+"&gradeTypeIds="
					+gradeTypeIds+"&grade.id="+gradeId
			});
    }
</script>
[@b.foot/]