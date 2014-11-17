[#ftl]
[@b.head/]
[@b.toolbar title="教学班成绩录入"]
	bar.addClose();
[/@]
[#if (gradeInputSwitch.startAt)??]
<link href="${base}/static/css/tab.css" rel="stylesheet" type="text/css">
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/common/TabPane.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/StringUtils.js"></script>
<style>
.gradePanel{
    background-color :#c7dbff;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
}

</style>
[@b.messages slash="5"/]
<br/>
    <table class="gradePanel" align="center" style="text-align:center;width:95%">
        <tr>
            <td align="center">
                <table style="padding:2%;width:100%">
                    <tr>
                        <td width="100px">课程序号:</td>
                        <td width="150px">${lesson.no}</td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>课程名称:</td>
                        <td>${lesson.course.name}</td>
                        <td>
	                        [#if (gradeInputSwitch.startAt)??]允许录入:
	                        	[#list gradeInputSwitch.types?sort_by("code") as gradeType]
	                        	${gradeType.name}
	                        	[/#list]
	                        [#else]
	                        	未开放录入
	                        [/#if]
                       	</td>
                    </tr>
                    [#if gradeState.confirmed]
                    <tr>
                        <td>成绩记录方式:</td>
                        <td>${gradeState.scoreMarkStyle.name}</td>
                        <td>
                        	[#if (gradeInputSwitch.startAt)??]
                        		开始时间:${gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm")}
                        	[/#if]
                        </td>
                    </tr>
                    <tr>
                        <td>成绩精确度:</td>
                        <td>${(gradeState.precision == 0)?string("保留整数", "保存一位小数")}</td>
                    [#else]
                    <tr>
                        <td>成绩记录方式:</td>
                        <td>
                        	[@b.select id="markStyleId"  name="markStyleId" items=markStyles value=(gradeState.scoreMarkStyle.id)?if_exists style="width:150px"/]
                        </td>
                        <td>[#if (gradeInputSwitch.startAt)??]开始时间:${gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
                    </tr>
                    <tr>
                        <td>成绩精确度:</td>
                        <td>
                        	[#assign scoreAccuracy = {'0':'保留整数','1':'保存一位小数'}/]
	                        [@b.select id="precision" name="precision" items=scoreAccuracy value=(gradeState.precision)?if_exists style="width:150px"/]
                        </td>
                    [/#if]
                        <td>[#if (gradeInputSwitch.startAt)??]截止时间:${gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
                    </tr>
                    <tr>
                        <td colspan="3">
                        	[#macro large_stateinfo(status)]
                        		[#if (status>0)]
                            		<td width="25px"><image src="${base}/static/themes/default/images/dialog-ok-apply.png" width="25px"/></td>
                            		<td width="50px">[#if status==1]已提交[#else]已发布[/#if]</td>
                            	[#else]
                            		<td></td>
                            	[/#if]
                           	[/#macro]
                        	[#macro small_stateinfo(status)]
                        		[#if (status>0)]
                            		<td width="15px"><image src="${base}/static/themes/default/images/dialog-ok-apply.png" width="15px"/></td>
                            	<td width="60px">[#if status==1]已提交[#else]已发布[/#if]</td>
                            	[#else]
                            		<td></td>
                            	[/#if]
                            [/#macro]
                            [#macro gradeInfoHTML(url, tdStyle1, tdStyle2, onclick, caption, iconSize, width1, width2)]<td style="${tdStyle1!}" width="${(!width1?? || width1 == "")?string("20px", width1?string)}"><image src="${base}/static/themes/default/images/dialog-information.png" width="${(!iconSize?? || iconSize == "")?string("15px", iconSize?string)}" onclick="${onclick}" class="padding"/></td><td style="${tdStyle2!}" width="${(!width2?? || width2 == "")?string("20px", width2?string)}"><a href="${url}" [#if onclick?length>0]onclick="${onclick}"[/#if]>${caption?default("查看")}</a></td>[/#macro]
                            [#macro inputHTML(url, tdStyle1, tdStyle2, onclick, caption, iconSize, width1, width2)]<td style="${tdStyle1!}" width="${(!width1?? || width1 == "")?string("20px", width1?string)}"><image src="${base}/static/themes/default/images/system-log-out1.png" width="${(!iconSize?? || iconSize == "")?string("15px", iconSize?string)}" onclick="${onclick}" class="padding"/></td><td style="${tdStyle2!}" width="${(!width2?? || width2 == "")?string("20px", width2?string)}"><a href="${url}" onclick="${onclick}">${caption?default("录入")}</a></td>[/#macro]
                            [#macro removeGradeHTML(url,onclick, tdStyle, caption, iconSize, width1, width2)]<td style="${tdStyle!}" width="${(!width1?? || width1 == "")?string("20px", width1?string)}"><image src="${base}/static/themes/default/images/edit-delete.png" width="${(!iconSize?? || iconSize == "")?string("15px", iconSize?string)}" onclick="${onclick}" class="padding"/></td><td style="${tdStyle!}" width="${(!width2?? || width2 == "")?string("50px", width2?string)}"><a href="${url}" onclick="${onclick}">${caption?default("删除")}</a></td>[/#macro]
                            [#macro printHTML(url,onclick, tdStyle)]<td style="${tdStyle!}"><image src="${base}/static/themes/default/images/printer.png"  width="20px" onclick="${onclick}" class="padding"/></td><td style="${tdStyle!}"><a href="${url}" onclick="${onclick}">打印</a></td>[/#macro]
                            [#if gaGradeTypes?size>0]
                            	[#include "inputGaPanel.ftl"/]
                            [/#if]
                            
                            [#if gradeInputSwitch.types?seq_contains(MAKEUP)]
                            <hr>
                            [#include "inputMakeupPanel.ftl"/]
                            [/#if]
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
    [#else]
    		[@b.div style="margin-top:10px;"]成绩还未开放录入![/@]
    [/#if]
[@b.foot/]