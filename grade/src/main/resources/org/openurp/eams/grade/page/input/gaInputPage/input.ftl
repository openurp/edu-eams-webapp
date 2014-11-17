[#ftl]
[@b.head/]
<link href="${base}/static/styles/grade-input-page.css" rel="stylesheet" type="text/css">
<script src="${base}/static/scripts/underscore.min.js" type="text/javascript"></script>
<script src="${base}/static/scripts/backbone.min.js" type="text/javascript"></script>
<script src="${base}/static/scripts/grade/course/new-input.js" type="text/javascript"></script>

[#function contains collection entity]
	[#list collection as e]
		[#if e.id == entity.id]
			[#return true /]
		[/#if]
	[/#list]
	[#return false]
[/#function]
<div align="center" class="grade-input-header">
	${lesson.semester.schoolYear!}学年${(lesson.semester.name)?if_exists?replace('0','第')}学期
    [#if students?size == 0]
    <br/>当前没有可以录入成绩的学生
    [/#if]
</div>
    
<div class="ui-widget" style="">
	<div class="actionMessage">
	  <div id="grade-input-prompt" class="ui-state-highlight ui-corner-all"> 
	    <span style="float: left; margin-right: 0.3em;" class="ui-icon ui-icon-info"></span>
	    <span>&nbsp;</span>
	  </div>
	</div>
</div>

<table align="center" border="0" class="grade-input-lesson-info">
    <tr>
        <td width="33%">${b.text("attr.courseNo")}:${lesson.course.code}</td>
        <td width="33%">${b.text("attr.courseName")}:${lesson.course.name}[#if lesson.subCourse??]<sup style="color:#513BC4" >${lesson.subCourse.name}</sup>[/#if]</td>
        <td align="left">${b.text("entity.courseType")}:${lesson.courseType.name}</td>
    </tr>
   	<tr>
        <td>${b.text("attr.taskNo")}:${(lesson.no)?if_exists}</td>
        <td>${b.text("task.courseSchedule.primaryTeacher")}:[#list lesson.teachers as t]${t.name}[#if t_has_next],[/#if][/#list]</td>
        <td>
        	${b.text("grade.recordMode")}:
            <input type="radio" name="tabIndexOrder" id="tabIndexOrder-1" value="BY_STD" checked>
            <label for="tabIndexOrder-1">${b.text("grade.recordModeByStd")}</label>
            <input type="radio" name="tabIndexOrder" id="tabIndexOrder-0" value="BY_GRADE_TYPE" >
            <label for="tabIndexOrder-0">${b.text("grade.recordModeByScore")}</label>
        </td>
    </tr>
   	<tr>
        <td>所录成绩:[#list inputableGradeTypes?sort_by("code") as gradeType]${gradeType.name}&nbsp;[#if (courseGradeState.getPercent(gradeType)?string.percent)??](${courseGradeState.getPercent(gradeType)?string.percent})[/#if][/#list]</td>
        <td>成绩精确度:[#if courseGradeState.precision=0]${b.text('grade.precision0')}[/#if][#if courseGradeState.precision=1]${b.text('grade.precision1')}[/#if]</td>
        <td></td>
    </tr>
    <tr>
    	<td colspan="3" class="grade-input-submit-button"><input type="button" value="提交成绩" /></td>
    </tr>
</table>

<table class="gridtable" style="width:98%" align="center">
	<thead class="gridhead">
		<tr>
			<th colspan="2">学生</th>
			[#list gradeTypeRange as gradeType]
			<th colspan="3">${gradeType.name}</th>
			[/#list]
			<th>总评成绩</th>
		</tr>
		<tr>
			<th>学号</th>
			<th>姓名</th>
			[#list gradeTypeRange as gradeType]
			<th>考试类型</th>
			<th>考试情况</th>
			<th>成绩</th>
			[/#list]
			<th>成绩</th>
		</tr>
	</thead>
	<tbody>
		[#list students as std]
		<tr class="griddata-even std-grades-${std.id}">
			<td>${std.code}</td>
			<td>${std.name}</td>
			[#list gradeTypeRange as gradeType]
				[#-- 这个成绩类型不在本次可录入的成绩类型范围内 --]
				[#if contains(uninputableGradeTypes, gradeType)]
					[#assign examGrade = stdId_gradeTypeId2uninputableExamGrade[std.id?string + "-" + gradeType.id?string]! /]
					<td>${(examGrade.examType.name)!}</td>
					<td>${(examGrade.examStatus.name)!}</td>
					<td>${(examGrade.scoreText)!}</td>
				[/#if]
				[#if contains(inputableGradeTypes, gradeType)]
					[#assign examGrade = stdId_gradeTypeId2inputableExamGrade[std.id?string + "-" + gradeType.id?string]! /]
					[#if !(examGrade.id)??]
					<td colspan="3">无考试记录，无法录入成绩</td>
					[#else]
					<td>${(examGrade.examType.name)!}</td>
					<td>
						[#assign statusSwitch = examGradeId2statusSwitch[examGrade.id?string] /]
						[#if statusSwitch.modifable ]
							<select name="examGrade-${examGrade.id}.examStatus.id">
							[#list baseCodes_examStatues as ex]
								<option value="${ex.id}"[#if examGrade.examStatus.id==ex.id] selected[/#if]>${ex.name}</option>
							[/#list]
							</select>
						[#else]
							${(examGrade.examStatus.name)!}
							<input type="hidden" name="examGrade-${examGrade.id}.examStatus.id" value="${(examGrade.examStatus.id)!}" />
						[/#if]
					</td>
					<td>
						[#assign scoreMarkStyle = courseGradeState.getState(gradeType).scoreMarkStyle /]
						[#assign scoreSwitch = examGradeId2scoreSwitch[examGrade.id?string] /]
						[#assign showScoreField = examGrade.examStatus.inputable /]
						[#if scoreSwitch.modifable]
							[#if scoreMarkStyle.numStyle]
								<input 
									type="text" name="examGrade-${examGrade.id}.score" value="${(examGrade.score)!}" 
									tabindex="${(std_index + 1) * 10 + gradeType_index}"
									tabindex_std="${(std_index + 1) * 10 + gradeType_index}"
									tabindex_grade_type="${(gradeType_index + 1) * 1000 + std_index} "
									maxlength="3" style="width:80%;[#if !showScoreField]display:none[/#if]" />
							[#else]
								<select name="examGrade-${examGrade.id}.score" style="width:80%;[#if !showScoreField]display:none[/#if]">
									<option value="">...</option>
									[#list scoreMarkStyleId2RateConfig[scoreMarkStyle.id?string].items?sort_by('defaultScore')?reverse as item]
							        	<option value="${item.defaultScore}" [#if (examGrade.score)?? && examGrade.score == item.defaultScore ]selected[/#if]>${item.grade}</option>
							        [/#list]
								</select>
							[/#if]
						[#else]
							${(examGrade.scoreText)!}
							<input type="hidden" name="examGrade-${examGrade.id}.score" value="${(examGrade.score)!}" />
						[/#if]
					</td>
					[/#if]
				[/#if]
			[/#list]
			<td class="result-area [#if !(stdId2ga[std.id?string].passed)!(false)]grade-input-unpassed[/#if]">
			${(stdId2ga[std.id?string].scoreText)!}
			</td>
		</tr>
		[/#list]
	</tbody>
</table>

<script>
jQuery(function() {
	/*
	事件列表:
	显示信息		grade:info
	显示错误信息	grade:error
	提交成绩前		grade:before-submit
	*/
	var eventBus = _.extend({}, Backbone.Events);
	
	new PromptView({
		"el" : jQuery("#grade-input-prompt"),
		"eventBus" : eventBus
	});
	var scoreFields = jQuery("[name^='examGrade-'][name$='.score']");
	_.sortBy(scoreFields, function(a) {
		var at = jQuery(a).prop("tabindex");
		at = at ? at : "-1";
		return parseInt(at, 10);
	});
	
	jQuery(":radio[name=tabIndexOrder]").click(function() {
		if("BY_STD" == jQuery(this).val()) {
			eventBus.trigger("grade:tabIndex-order-by-std")
		} else {
			eventBus.trigger("grade:tabIndex-order-by-grade-type")
		}
		_.sortBy(scoreFields, function(a) {
			var at = jQuery(a).prop("tabindex");
			at = at ? at : "-1";
			return parseInt(at, 10);
		});
	});
	
	var examStatuses = {};
	[#list baseCodes_examStatues as ex]
		examStatuses["${ex.id}"] = { inputable : ${ex.inputable?string("true","false")}};
	[/#list]
	[#list students as std]
	new StdGradeView({
		"el" : jQuery(".std-grades-${std.id}"),
		"eventBus" : eventBus,
		"examStatuses" : examStatuses,
		"lessonId" : ${lesson.id},
		"saveAjaxURL" : "${b.url("!saveGAAjax")}",
		"scoreFields" : scoreFields
	});
	[/#list]
	
	jQuery(".grade-input-submit-button").click(function(event) {
		event.preventDefault();
		var result = { passed : true };
		eventBus.trigger("grade:before-submit", result);
		if (result.passed) {
			bg.Go("${b.url("!submitGA?lesson.id=" + lesson.id)}");
		} else {
			alert("请录入成绩后再提交");
		}
	});
});
</script>
[@b.foot/]