[#ftl]
[@b.head/]
<script src="${base}/static/scripts/underscore.min.js" type="text/javascript"></script>

[#assign grade=courseGrade]
[#assign gradeStatus = {'0':'新添加','1':'已提交','2':'已发布'}/]
[@b.form name="courseGradeForm" action="!save" targe="contentDiv"]
<input type="hidden" name="courseGrade.id" value="${grade.id!}"/>

<table class="infoTable" width="100%">
    <tr>
     <td class="title">${b.text('attr.stdNo')}</td>
     <td class="content">${grade.std.code!}</td>
     <td class="title">${b.text('attr.personName')}</td>
     <td class="content">${grade.std.name!}</td>
     <td class="title">${b.text('department')}</td>
     <td class="content">${grade.std.department.name!}</td>
    </tr>
    <tr>
        <td class="title">${b.text('attr.taskNo')}</td>
        <td class="content">${(grade.lesson.no)!}</td>
        <td class="title">${b.text('attr.courseNo')}</td>
        <td class="content">${grade.course.code!}</td>
        <td class="title">${b.text('attr.courseName')}</td>
        <td class="content">${grade.course.name!}[#if grade.subCourse??]<sup style="color:#3B50AE;">${grade.subCourse.name!}</sup>[/#if]</td>
    </tr>
    <tr>
        <td class="title">学年学期</td>
        <td class="content">${grade.semester.schoolYear!}学年${(grade.semester.name)?if_exists?replace("0","第")}学年</td>
        <td class="title">${b.text('attr.credit')}</td>
        <td class="content">${(grade.course.credits)?if_exists}</td>
        <td class="title">绩点</td>
        <td class="content">${(grade.gp?string("#.##"))?if_exists}</td>
    </tr>
    <tr>
        <td class="title">是否通过</td>
        <td class="content">${(grade.passed)?if_exists?string("是","<font color='red'>否</font>")}</td>
        <td class="title">状态</td>
        <td class="content">
        [@b.select  items=gradeStatus name="courseGrade.status"  value="${grade.status?default('0')}"/]
        </td>
        <td class="title">${b.text('entity.markStyle')}</td>
        <td>[@b.select items=markStyles value=(courseGrade.markStyle.id)?if_exists name="courseGrade.markStyle.id"/]</td>
    </tr>
    <tr>
        <td class="title">得分</td>
        <td class="content" id="courseGrade.score.container">
            [@editScore grade "courseGrade.score"/]
            <input type="checkbox" name="updateGrade" id="updateGrade"/>是否修改
        </td>
        <td class="title">${b.text('entity.courseType')}</td>
        <td class="content">
        	[@b.select items=courseTypes name="courseGrade.courseType.id" style="width:90%" value=(grade.courseType.id)?if_exists/]
        </td>
        <td class="title">修读类别</td>
        <td class="content">
        	[@b.select items=courseTakeTypes name="courseGrade.courseTakeType.id" value=(grade.courseTakeType.id)?default("1")/]
        </td>
    </tr>
    <tr>
        <td class="title">创建时间</td>
        <td class="content">${(grade.createdAt?string('yyyy-MM-dd HH:mm'))!}</td>
        <td class="title">修改时间</td>
        <td class="content" colspan="3">${(grade.updatedAt?string('yyyy-MM-dd HH:mm'))!}</td>
    </tr>
</table>
	[@b.grid items=courseGrade.examGrades?sort_by(["gradeType","code"]) var="examGrade" sortable="false"]
		[@b.row]
			[@b.col title="成绩种类" id="f_gradeType${examGrade.gradeType.id}"]<input name="gradeTypeId" type="hidden" value="${examGrade.gradeType.id}"/>${examGrade.gradeType.name!}[/@]
			[@b.col title="考试类型"]
				[@b.select items=examTypes value=(examGrade.examType.id)?if_exists name="examTypeId${examGrade.gradeType.id}" empty="..."/]
			[/@]
			[@b.col title="考试情况"]
				[@b.select items=examStatuses value=(examGrade.examStatus.id)?if_exists name="examStatusId${examGrade.gradeType.id}" empty="..."/]
			[/@]
			[@b.col title="记录方式"]
				[@b.select items=markStyles value=(examGrade.markStyle.id)?if_exists name="markStyleId${examGrade.gradeType.id}" empty="..."/]
			[/@]
			[@b.col title="得分" width="20%"]
				[@editScore examGrade "score${examGrade.gradeType.id}"/]
			[/@]
			[@b.col title="百分比"]
				[#if gradeState?exists]${(examGrade.percent + "%")!}[/#if]
			[/@]
			[@b.col title="是否通过"]
				[#if examGrade.id??]${examGrade.passed?string("是", "<font color='red'>否</font>")}[/#if]
			[/@]
			[@b.col title="状态"]
				[@b.select name="status${examGrade.gradeType.id}" items=gradeStatus value="${examGrade.status}" /]
			[/@]
		[/@]
	[/@]
[/@]

[#macro editScore grade name]
    [#if grade.markStyle.numStyle || !markStyleId2RateConfig[grade.markStyle.id?string]?exists]
		<input type="text" name="${name}" id="${name}" style="width:50px" value="${(grade.score?string("#.##"))?if_exists}" />
    [#else]
    	[#if markStyleId2RateConfig[grade.markStyle.id?string]??]
			<select name="${name}" id="${name}" style="width:50px">
			    <option value="">...</option>
	            [#list markStyleId2RateConfig[grade.markStyle.id?string].items as item]
			    <option value="${item.defaultScore}" [#if grade.score??][#if item.contains(grade.score)]selected[/#if][/#if]>${item.grade}</option>
	            [/#list]
			</select>
        [/#if]
    [/#if]
	<input type="hidden" name="placeholder_${name}" />
[/#macro]

[#-- 各种成绩记录方式的模板 --]
[#list markStyles as markStyle]
<script type="text/template" id="markStyle-template-${markStyle.id}">
	[#if markStyle.numStyle]
		<input type="text" name="<%= name %>" id="<%= name %>" style="width:50px" value="" />
	[#else]
		[#if markStyleId2RateConfig[markStyle.id?string]??]
			<select name="<%= name %>" id="<%= name %>"  style="width:50px">
			    <option value="">...</option>
           		[#list markStyleId2RateConfig[markStyle.id?string].items as item]
			    <option value="${item.defaultScore}">${item.grade}</option>
	            [/#list]
			</select>
		[#else]
			<input type="text" name="<%= name %>" id="<%= name %>" style="width:50px" value="" />
		[/#if]
	[/#if]
	<input type="hidden" name="placeholder_<%= name %>" />
</script>
[/#list]

<script>
jQuery(function() {
	jQuery("#updateGrade").change(function(event) {
		if(jQuery(event.target).is(":checked")) {
			jQuery("#courseGrade\\.score").removeAttr("disabled");
		} else {
			jQuery("#courseGrade\\.score").attr("disabled", "disabled");
		}
	});
	
	// 修改考试成绩记录方式的时候，同时修改得分输入框
	jQuery("select[name^=markStyleId]").unbind("change").change(function(event) {
		var $markStyleSelect = jQuery(event.target);
		var markStyleId = $markStyleSelect.val();
		var gradeTypeId = $markStyleSelect.attr("name").replace(/markStyleId/g, '');
		var scoreInputName = "placeholder_score" + gradeTypeId;
		var $scoreInputContainer = jQuery("[name=" + scoreInputName + "]").parent();
		$scoreInputContainer.find(":text,select").remove();
		if(markStyleId != "") {
			var tmpl = _.template(jQuery("#markStyle-template-" + markStyleId).html());
			$scoreInputContainer.html(tmpl({"name":scoreInputName.replace(/placeholder_/g,'')}));
			checkExamGradeNumberFormat();
		}
	});
	
	// 修改课程成绩记录方式的时候，同时修改得分输入框
	jQuery("select[name='courseGrade.markStyle.id']").unbind("change").change(function(event) {
		var $markStyleSelect = jQuery(event.target);
		var markStyleId = $markStyleSelect.val();
		var $scoreInputContainer = jQuery("#courseGrade\\.score\\.container");
		$scoreInputContainer.find(":text,select").remove();
		
		var tmpl = _.template(jQuery("#markStyle-template-" + markStyleId).html());
		$scoreInputContainer.prepend(tmpl({"name" : "courseGrade.score"}));
		
		checkCourseGradeNumberFormat();
		
		jQuery("#updateGrade").attr("checked", "checked");
	});
	
	jQuery("#updateGrade").change();
	
	// 考试成绩的数字检验
	var checkCourseGradeNumberFormat = function() {
		jQuery(":text[name=courseGrade\\.score]")
			.unbind("change")
			.change(function(event) {
				if(isNaN(this.value)) {
					alert("请输入数字");
					this.value="";
				}
			});
	};
	
	// 考试成绩的数字检验
	var checkExamGradeNumberFormat = function() {
		jQuery("form[name=courseGradeForm] :text[name^=score]")
			.unbind("change")
			.change(function(event) {
				if(isNaN(this.value)) {
					alert("请输入数字");
					this.value="";
				}
			});
	};
	checkExamGradeNumberFormat();
	checkCourseGradeNumberFormat();
});
</script>
[@b.foot/]