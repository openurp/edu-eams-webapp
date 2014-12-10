[#ftl]
[#macro i18nName(object)]
  [#if object.enName??]${object.enName}[#else]${object.name}[/#if]
 [/#macro]
[#--这个是系统的打印模板--]
[@b.head/]
<style type="text/css">
.reportTable {
  border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
    font-size: 10pt; 
}
table.reportTable td{
  border:solid;
  border-width:0px;
  border-right-width:1px;
  border-bottom-width:1px;
  border-color:#006CB2;
}
.printTableStyle {
  border-collapse: collapse;
    border:solid;
  border-width:2px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
  font-size: 10pt; 
}
table.printTableStyle td{
  border:solid;
  border-width:0px;
  border-right-width:2px;
  border-bottom-width:2px;
  border-color:#006CB2;
        height:20px;
}
</style>
[@b.toolbar title="教学班成绩打印"]
  bar.addPrint();
    bar.addItem("${b.text('action.export')}","exportData()");
    bar.addBackOrClose();
    //bar.addItem("成绩分段统计",action.method("statTask"));
[/@]
[#macro i18nName value]${value}[/#macro]
[#macro getTeacherNames teachers][#list teachers as teacher][#if teacher_index gt 0]、[/#if]${teacher.name!}[/#list][/#macro]
[#macro displayGrades(index,grade,gradeTypes)]
    <td style="text-align:center">${index+1}</td>
    <td>${grade.std.code}</td>
    <td>${grade.std.person.name}</td>
    [#list gradeTypes as gradeType]
       <td>${grade.getGrade(gradeType).score!}</td>
    [/#list]
    <td></td>
[/#macro]
<div id = "DATA" width="100%" align="center" cellpadding="0" cellspacing="0">
[#assign pageSize=80]
[#list lessonGradeReports as report]
  [#assign grades=report.grades]
  [#assign pages=(grades?size/pageSize)?int /]
  [#if grades?size==0][#break][/#if]
  [#if (pages*pageSize<grades?size)][#assign pages=pages+1][/#if]
  [#assign teachTask = report.lesson]


  [#list 1..pages as page]
    <div align='center'><h3>${teachTask.project.school.name}课程成绩登记表</h3></div>
    <div align='center'>${(teachTask.semester.schoolYear)!}年度${(teachTask.semester.name?replace('0','第'))?if_exists}学期</div>
    <table width='100%' align='center' border='0' style="font-size:13px">
      <tr>
        <td width='25%'>课程代码:${teachTask.course.code}</td>
        <td width='40%'>课程名称：${teachTask.course.name}</td>
        <td align='left'>课程类别:${teachTask.courseType.name}</td>
      </tr>
      <tr>
        <td>课程序号:${teachTask.no?if_exists}</td>
        <td>主讲教师:[#list teachTask.teachers as lsteacher][#if lsteacher_index gt 0]、[/#if]${lsteacher.name!}[/#list]</td>
        <td align='left'>授课院系:${(teachTask.teachDepart.name)!}</td>
      </tr> 
      [#--
      <tr>
          <td colspan="3">[#list report.gradeTypes as gradeType][#if gradeType.id != GA && gradeType.id != FINAL][#if teachTask.gradeState.getPercent(gradeType)?exists && teachTask.gradeState.getPercent(gradeType)?number != 0][@i18nName gradeType/]${teachTask.gradeState.getPercent(gradeType)?string.percent}　[/#if][/#if][/#list]</td>
      </tr>
      --]
    </table>
    <table align="center" class="reportTable" style="table-layout: fixed">
      <tr align="center">
      [#list 1..2 as i]
          <td align="center" width="60px">序号</td>
          <td align="center" width="90px">学号</td>
          <td width="150px">姓名</td>
          [#list report.gradeTypes as gradeType]
          [#--[#assign gradeTypeName][@i18nName gradeType/][/#assign]--]  
          <td width="80px">${(gradeType.name)!}</td>
          [/#list]
        <td align="center" width="80px">备注</td>
        [/#list]
        </tr>
        [#list 0..(pageSize/2-1) as i]
          <tr>
          [#assign j=i+(page-1)*pageSize]
          [#if grades[j]?exists]
          [@displayGrades j,grades[j],report.gradeTypes/]
          [#else]
              [#break]
          [/#if]
          [#assign j=i+(page-1)*pageSize+(pageSize/2)]
          [#if grades[j]?exists]
            [@displayGrades j,grades[j],report.gradeTypes/]
          [#else]
            [#list 1..(4+report.gradeTypes?size) as i]
                <td>&nbsp;</td>
            [/#list]
          [/#if]
          </tr>
        [/#list]
     </table>
  [#--   <table width='100%' align='center' border='0' style="font-size:13px;vertical-align: center">
      <td>任课教师签名:</td>
      <td width="15%">　　　年　　　月　　　日</td>
      <td width="5%"></td>
    </tr> 
   </table>
    --] 
    <tr height="30px">

    <table class="printTableStyle" width="100%">
        <tr  width="100%">
         <td rowspan='7' width="40%">
         实际参加考试考查人数____________人<br>
         缺&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;考&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;人&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数____________人<br>
         教师签名:_______________________<br>
         系主任签名:_____________________<br>
         <br>
         日期:_____________年_____月_____日
         </td>
         <td colspan='3' width="60%" align="center">总评成绩分段统计</td>
        <tr>
         <td width="30%"  align="center">成绩</td>
         <td width="15%"  align="center">人数</td>
         <td width="15%"  align="center">百分比</td>
         </tr>
         [#list lglist as lg]
        <tr>
         <td align="center">${lg.min}-${lg.max}(${lg.name})</td>
         <td>${lg.count}</td>
         <td align="right">${(lg.count/grades?size)?string.percent}</td>
        </tr>
        [/#list]
     [#--
         <td align="center">80-89.9(良)</td>
         <td></td>
         <td align="right">%</td>
        <tr>
         <td align="center">70-79.9(中)</td>
         <td></td>
         <td align="right">%</td>
        <tr>
         <td align="center">60-69.9(及格)</td>
         <td></td>
         <td align="right">%</td>
        <tr>
         <td align="center">0-59.9(不及格)</td>
         <td></td>
         <td align="right">%</td>
        </tr>
     --]
        </table>  
     [#if page_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]
     [/#list]
   [#if report_has_next]
     <div style='PAGE-BREAK-AFTER: always'></div> 
   [/#if]
[/#list] 
</div>
<script>
   function exportData(){
       [#if Parameters['lessonIds']?exists]
       self.location="teachClassGradeReport!export.action?template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonIds']}";
       <#--该页面可能从单个成绩的录入跳转过来-->
       [#elseif Parameters['lessonId']?exists]
       self.location="teachClassGradeReport!export.action?template=teachClassGradeReport.xls&lessonIds=${Parameters['lessonId']}";
       [/#if]
   }
</script>
[@b.foot/]