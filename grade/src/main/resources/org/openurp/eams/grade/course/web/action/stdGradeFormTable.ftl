[#ftl]
[@b.head/]
[#assign grade=courseGrade]
[#assign gradeStatus = {'0':'新添加','1':'录入确认','2':'已发布'}/]
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
        <td class="content">${grade.lessonNo?if_exists}</td>
        <td class="title">${b.text('attr.courseNo')}</td>
        <td class="content">${grade.course.code!}</td>
        <td class="title">${b.text('attr.courseName')}</td>
        <td class="content">${grade.course.name!}</td>
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
        <td class="title" id="f_final">得分</td>
        <td class="content" id="finalScoreTd">
            [@editScore grade "courseGrade.score"/]
            <input type="checkbox" name="updateGrade" id="updateGrade" onchange="updateGradeControl('courseGrade.score');"/>是否修改
        </td>
        <td class="title">${b.text('entity.courseType')}</td>
        <td class="content">
        	[@b.select items=courseTypes name="courseGrade.courseType.id" value=(grade.courseType.id)?if_exists/]
        </td>
        <td class="title">修读类别</td>
        <td class="content">
        	[@b.select items=courseTakeTypes name="courseGrade.courseTakeType.id" value=(grade.courseTakeType.id)?default("1")/]
        </td>
    </tr>
    <tr>
        <td class="title">创建时间</td>
        <td class="content">${grade.createdAt!}</td>
        <td class="title">修改时间</td>
        <td class="content" colspan="3">${grade.updatedAt!}</td>
    </tr>
</table>
	[@b.grid items=courseGrade.examGrades?sort_by(["gradeType","code"]) var="examGrade" sortable="false"]
		[@b.row]
			[@b.col title="成绩种类" id="f_gradeType${examGrade.gradeType.id}"]<input name="gradeTypeId" type="hidden" value="${examGrade.gradeType.id}"/>${examGrade.gradeType.name!}[/@]
			[@b.col title="考试情况"][@b.select items=examStatuses value=(examGrade.examStatus.id)?if_exists name="examStatusId${examGrade.gradeType.id}" empty="..."/][/@]
			[@b.col title="记录方式"][@b.select items=markStyles value=(examGrade.markStyle.id)?if_exists name="markStyleId${examGrade.gradeType.id}" empty="..."/][/@]
			[@b.col title="得分" width="20%"][@editScore examGrade "score${examGrade.gradeType.id}"/][/@]
			[@b.col title="百分比"][#if gradeState?exists]${(gradeState.getPercent(examGrade.gradeType)?string.percent)?if_exists}[/#if][/@]
			[@b.col title="是否通过"][#if examGrade.id??]${examGrade.passed?string("是", "<font color='red'>否</font>")}[/#if][/@]
			[@b.col title="状态"][@b.select name="status${examGrade.gradeType.id}" items=gradeStatus value="${examGrade.status}" /][/@]
		[/@]
	[/@]
[/@]

[#macro editScore grade name]
    [#if grade.markStyle.numStyle || !converter.getConfig(project,grade.markStyle)?exists]
		<input type="text" name="${name}" id="${name}" style="width:50px" value="${(grade.score?string("#.##"))?if_exists}" />
    [#else]
        [#if converter.getConfig(project,grade.markStyle)??]
			<select name="${name}" id="${name}" >
			    <option value="">...</option>
	            [#list converter.getConfig(project,grade.markStyle).items as item]
			    <option value="${item.defaultScore}" [#if grade.score??][#if item.contains(grade.score)]selected[/#if][/#if]>${item.grade}</option>
	            [/#list]
			</select>
        [/#if]
    [/#if]
[/#macro]


<script>
    function updateGradeControl(name){
        document.getElementById(name).disabled = !document.getElementById("updateGrade").checked;
    }
   //$("updateGrade").onchange();
</script>
[@b.foot/]