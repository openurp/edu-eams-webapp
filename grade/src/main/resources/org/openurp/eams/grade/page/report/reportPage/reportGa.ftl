[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#assign perRecordOfPage = 70/]
[#include "reportMacros.ftl"/]
[@reportStyle/]
[@b.toolbar title="教学班成绩打印"]
	 bar.addPrint();
	 bar.addClose();
[/@]
[#list reports as report]
	[#if report.courseGradeState??]
    [#assign recordIndex = 0/]
    [#--按页循环一组成绩--]
    [#assign pageSize = ((report.courseGrades?size / perRecordOfPage)?int * perRecordOfPage == report.courseGrades?size)?string(report.courseGrades?size / perRecordOfPage, report.courseGrades?size / perRecordOfPage + 1)?number/]
    [#list (pageSize == 0)?string(0, 1)?number..pageSize as pageIndex]
    [@gaReportHead report/]
    <table align="center" class="reportBody" width="95%">
       [@reportColumnTitle report/]
       [#list 0..(perRecordOfPage / 2 - 1) as onePageRecordIndex]
       <tr>
		[@displayGaGrade report, recordIndex/]
		[@displayGaGrade report, recordIndex + perRecordOfPage / 2/]
        [#assign recordIndex = recordIndex + 1/]
       </tr>
       [/#list]
       [#assign recordIndex = perRecordOfPage * pageIndex/]
    </table>
		[@gaReportFoot report/]
        [#if (pageIndex + 1 < pageSize)]
    <div style="PAGE-BREAK-AFTER: always"></div>
        [/#if]
    [/#list]
    [#if report_has_next]
    <div style="PAGE-BREAK-AFTER: always"></div>
    [/#if]
    [#else]
    	该课程没有学生成绩!
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