[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[#assign perRecordOfPage = 70/]
[#include "reportMacros.ftl"/]
[@reportStyle/]
[@b.toolbar title="教学班总评成绩打印"]
	 bar.addPrint();
	 bar.addClose();
[/@]
[#list reports as report]
	[#if report.courseGradeState??]
    [#assign recordIndex = 0/]
    [#assign pageSize = ((report.courseGrades?size / perRecordOfPage)?int * perRecordOfPage == report.courseGrades?size)?string(report.courseGrades?size / perRecordOfPage, report.courseGrades?size / perRecordOfPage + 1)?number/]
    [#list (pageSize == 0)?string(0, 1)?number..pageSize as pageIndex]
    [@gaReportHead report/]
    [#assign totalNormal=0/]
    [#assign totalNormalScore=0/]
    [#list report.courseGrades as courseGrade]
    	[#assign examGrade=courseGrade.getExamGrade(END)!"null"/]
    	[#if examGrade!="null" && (examGrade.examStatus.id!0)=1]
    		[#assign totalNormal=totalNormal + 1 /] [#assign totalNormalScore=totalNormalScore+(examGrade.courseGrade.getExamGrade(GA).score)!0/]
    	[/#if]
    [/#list]
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
   function exportData(){
       [#if Parameters['lessonIds']?exists]
       self.location="teachClassGradeReport.action?method=export&template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonIds']}";
       [#--该页面可能从单个成绩的录入跳转过来--]
       [#elseif Parameters['lessonId']?exists]
       self.location="teachClassGradeReport.action?method=export&template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonId']}";
       [/#if]
   }
   
   function toPrint() {
    if (confirm("打印前请设置一下“打印页边距”，依次点击\n菜单“文件”->“页面设置”，在弹出的页面\n中将“页边距”项中的“上”、“下”、\n左”、“右”的值设为 10 。\n\n是否已经设置好了？")) {
        print();
    }
   }
</script>
[@b.foot/]