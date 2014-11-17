[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[#assign gradeStatus={'0':'新添加','1':'已提交','2':'已发布'}/]
    <table width="100%" class="infoTable">
        <tr>
            <td class="title">${b.text("attr.taskNo")}:</td>
            <td class="content">${task.seqNo}</td>
            <td class="title">${b.text("attr.courseNo")}:</td>
            <td class="content">${task.course.code}</td>
            <td class="title">${b.text("attr.courseName")}:</td>
			<td class="content">[@i18nName task.course/]</td>
        </tr>
        <tr>
            <td class="title">授课教师:</td>
            <td class="content">${task.teachers.name}</td>
            <td class="title">考核方式:</td>
            <td class="content">${(task.course.ext.examMode.name)!}</td>
            <td class="title">上次录入</td>
			<td class="content">[#if gradeState.inputedAt??]${gradeState.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
        </tr>
        <tr>
            <td class="title">记录方式:</td>
            <td class="content">[@i18nName gradeState.markStyle/]</td>
            <td class="title">精确度(保留小数位):</td>
            <td class="content">${gradeState.precision}位</td>
            <td class="title">状态</td>
			<td class="content">${gradeStatus[gradeState.status?string]}</td>
        </tr>
    </table>
    <table class="listTable" width="100%">
        <tr align="center" class="darkColumn">
        	<td>成绩类型</td>
        	<td>记录方式</td>
        	<td>百分比</td>
        	<td>精确度(保留小数位)</td>
        	<td>状态</td>
        	<td>上次录入</td>
        </tr>
        [#list gradeState.states?sort_by(["gradeType","code"])  as state]
        <tr  align="center" >
        	<td>${state.gradeType.name}</td>
        	<td>[@i18nName state.markStyle/]</td>
        	<td>[#if state.percent??]${state.percent?string.percent}[/#if]</td>
        	<td>${state.precision}位</td>
        	<td>${gradeStatus[state.status?string]}</td>
        	<td>[#if state.inputedAt??]${state.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
        </tr>
        [/#list]
    </table>
[@b.foot/]