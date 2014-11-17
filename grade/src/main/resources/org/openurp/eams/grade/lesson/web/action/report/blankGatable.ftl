[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"] 
[#assign perRecordOfPage = 70/]
[#include "blankMacros.ftl"/]
[@reportStyle/]
[@b.toolbar title="教学班总评成绩登分表打印"]
	bar.addPrint();
	bar.addClose();
	 
	setPortrait(true);
	setTopMargin("5");
	setLeftMargin("5");
	setRightMargin("5");
	setBottomMargin("5");
[/@]
[#list lessons as lesson]
    [#assign recordIndex = 0/]
    [#assign courseTakes = courseTakeMap.get(lesson)?sort_by(["std","code"])/]
    [#assign pageSize = ((courseTakes?size / perRecordOfPage)?int * perRecordOfPage == courseTakes?size)?string(courseTakes?size / perRecordOfPage, courseTakes?size / perRecordOfPage + 1)?number/]
    [#list (pageSize == 0)?string(0, 1)?number..pageSize as pageIndex]
    [@gaReportHead lesson/]
    <table align="center" class="reportBody" width="95%">
       [@gaColumnTitle/]
       [#list 0..(perRecordOfPage / 2 - 1) as onePageRecordIndex]
       <tr>
		[@displayGaTake courseTakes, recordIndex/]
		[@displayGaTake courseTakes, recordIndex + perRecordOfPage / 2/]
        [#assign recordIndex = recordIndex + 1/]
       </tr>
       [/#list]
       [#assign recordIndex = perRecordOfPage * pageIndex/]
    </table>
		[@gaReportFoot lesson/]
        [#if (pageIndex + 1 < pageSize)]
    <div style="PAGE-BREAK-AFTER: always"></div>
        [/#if]
    [/#list]
    [#if lesson_has_next]
    <div style="PAGE-BREAK-AFTER: always"></div>
    [/#if]
[/#list]
[@b.foot/]