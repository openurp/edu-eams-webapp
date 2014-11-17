[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"] 
[#include "blankMacros.ftl"/]
[@reportStyle/]
[@b.toolbar title="教学班补缓登分表打印"]
	 bar.addPrint();
	 bar.addClose();
[/@]
[#assign perRecordOfPage = 50/]
[#list lessons as lesson]
    [#assign recordIndex = 0/]
    [#assign courseTakes = courseTakeMap.get(lesson)?sort_by(["std","code"])/]
    [#assign pageSize = ((courseTakes?size / perRecordOfPage)?int * perRecordOfPage == courseTakes?size)?string(courseTakes?size / perRecordOfPage, courseTakes?size / perRecordOfPage + 1)?number/]
    [#list (pageSize == 0)?string(0, 1)?number..pageSize as pageIndex]
    [@makeupReportHead lesson/]
    <table align="center" class="reportBody" width="95%">
       [@makeupColumnTitle/]
       [#list 0..(perRecordOfPage / 2 - 1) as onePageRecordIndex]
       <tr>
		[@displayMakeupTake courseTakes, recordIndex/]
		[@displayMakeupTake courseTakes, recordIndex + perRecordOfPage / 2/]
        [#assign recordIndex = recordIndex + 1/]
       </tr>
       [/#list]
       [#assign recordIndex = perRecordOfPage * pageIndex/]
    </table>
		[@makeupReportFoot lesson/]
        [#if (pageIndex + 1 < pageSize)]
    <div style="PAGE-BREAK-AFTER: always"></div>
        [/#if]
    [/#list]
    [#if lesson_has_next]
    <div style="PAGE-BREAK-AFTER: always"></div>
    [/#if]
[/#list]

<script language="JavaScript">   
   function toPrint() {
    if (confirm("打印前请设置一下“打印页边距”，依次点击\n菜单“文件”->“页面设置”，在弹出的页面\n中将“页边距”项中的“上”、“下”、\n左”、“右”的值设为 10 。\n\n是否已经设置好了？")) {
        print();
    }
   }
</script>
[@b.foot/]