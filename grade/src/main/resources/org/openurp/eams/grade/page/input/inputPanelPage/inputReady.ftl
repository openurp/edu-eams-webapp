[#ftl]
[@b.head/]
<link href="${base}/static/styles/grade-input-panel.css" rel="stylesheet" type="text/css">

[@b.toolbar title="教学班成绩录入"]
	bar.addClose();
[/@]

[#macro gradeStatusIcon gradeState]
	[#if gradeState.beyondSubmit]
		<image src="${base}/static/themes/default/images/icon-ok.png" width="25px"/>
		[#if gradeState.status==GRADE_STATE_STATUS_CONFIRMED]已提交[#else]已发布[/#if]
	[/#if]
[/#macro]

[#macro inputIcon icon]
	<image src="${base}/static/themes/default/images/${icon}" />
[/#macro]
[@b.messages slash="5"/]
    <table class="gradePanel" align="center" style="text-align:center;width:95%">
        <tr>
            <td align="center">
                <table style="padding:2%;width:100%">
                    <tr>
                        <td width="18%" style="text-align:right;">课程序号:</td>
                        <td width="10%" style="text-align:left;">${lesson.no}</td>
                        <td width="18%" style="text-align:right;">课程代码:</td>
                        <td width="18%" style="text-align:left;">${lesson.course.code}</td>
                        <td width="18%" style="text-align:right;">课程名称:</td>
                        <td width="18%" style="text-align:left;">${lesson.course.name}[#if lesson.subCourse??]<sup style="color:#184DA4">${lesson.subCourse.name}</sup>[/#if]</td>
                    </tr>
                    <tr>
                    [#if courseGradeState.beyondSubmit]
                        <td style="text-align:right;">成绩记录方式:</td>
                        <td style="text-align:left;">${courseGradeState.scoreMarkStyle.name}</td>
                        <td style="text-align:right;">成绩精确度:</td>
                        <td style="text-align:left;">${(courseGradeState.precision == 0)?string("保留整数", "保存一位小数")}</td>
                   		<td colspan="2"></td>
                    [#else]
                    	<td style="text-align:right;">成绩记录方式:</td>
                        <td style="text-align:left;">
                        	[@b.select id="markStyleId"  name="markStyleId" items=markStyles value=(courseGradeState.scoreMarkStyle.id)?if_exists style="width:150px"/]
                        </td>
                        <td style="text-align:right;">最终成绩精度:</td>
                        <td style="text-align:left;">
                        	[#assign scoreAccuracy = {'0':'保留整数','1':'保存一位小数'}/]
	                        [@b.select id="precision" name="precision" items=scoreAccuracy value=(courseGradeState.precision)?if_exists style="width:150px"/]
                        </td>
                    	<td colspan="2"></td>
                    </tr>
                    [/#if]
                    <tr>
                    	<td style="text-align:right;">录入开始时间:</td>
                    	<td style="text-align:left;">[#if (gradeInputSwitch.startAt)??]${gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
                    	<td style="text-align:right;">录入截止时间:</td>
                        <td style="text-align:left;">[#if (gradeInputSwitch.startAt)??]${gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
                        <td colspan="2"></td>
                    </tr>
                    <tr>
                    	<td style="text-align:right;">录入提示:</td>
                    	<td style="text-align:left;" colSpan="6">${(gradeInputSwitch.remark?html)!}</td>
                    </tr>
                    <tr>
                        <td colspan="6">
                    		[#include "/com/ekingstar/eams/teach/grade/page/input/gaInputPage/inputPanel.ftl"/]
                    		[#include "/com/ekingstar/eams/teach/grade/page/input/gaDelayInputPage/inputPanel.ftl" /]
                    		[#include "/com/ekingstar/eams/teach/grade/page/input/finalInputPage/inputPanel.ftl" /]
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
[@b.foot/]