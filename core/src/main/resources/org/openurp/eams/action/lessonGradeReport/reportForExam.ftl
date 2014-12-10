[#ftl]
[@b.head/]
[#macro i18nName(object)]
  [#if object.enName??]${object.enName}[#else]${object.name}[/#if]
 [/#macro]
[@b.toolbar title="试卷分析表"]
    bar.addPrint();
    bar.addClose();
[/@]
<style type="text/css">
body{
 font-family:楷体_GB2312;
 font-size:14px;
}
.reportTable {
  border-collapse: collapse;
    border:solid;
  border-width:1px;
    border-color:black;
    vertical-align: middle;
    font-style: normal; 
  font-family:楷体_GB2312;
  font-size:15px;
}
table.reportTable td{
  border:solid;
  border-width:1px;
  border-right-width:1;
  border-bottom-width:1;
  border-color:black;

}
</style>
<div id="DATA" width="100%">
[#macro getTeacherNames teachers][#list teachers as teacher][#if teacher_index gt 0]、[/#if]${teacher.name!}[/#list][/#macro]

[#list courseStats as courseStat]
  [#assign teachTask=courseStat.lesson]
  [@b.div style="text-align:center"]
    <h2>${teachTask.project.school.name}课程考核试卷分析表</h2>
    ${teachTask.semester.schoolYear!}学年${(teachTask.semester.name)?if_exists?replace("0","第")}学期
  [/@]
   <table align="center" width="95%" border='0' style="font-weight:bold;">
    <tr>
            <td>开课院(系、部):[@i18nName teachTask.teachDepart/]</td>
      <td colSpan='2'>主讲教师:[@getTeacherNames teachTask.teachers?if_exists/]</td>
    </tr>
    <tr>
      <td>课程序号:${teachTask.no?if_exists}</td>
        <td>课程名称:[@i18nName teachTask.course/]</td>
      <td>课程类别:[@i18nName teachTask.courseType/]</td>
    </tr>
   </table>
     [#list courseStat.gradeSegStats as gradeStat]
     <table width="95%" align="center" class="reportTable">
       <tr>
          <td rowspan="4">一、成绩分布</td>
          <td align="left">分数段</td>
          [#list gradeStat.scoreSegments as seg]
          <td align="center">${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
          [/#list]
       </tr>
       <tr align="center">
          <td align="left">人数</td>
          [#list gradeStat.scoreSegments as seg]
          <td>${seg.count}</td>
          [/#list]
       </tr>
       <tr align="center">
          <td align="left">比例数</td>
          [#list gradeStat.scoreSegments as seg]
          <td>${((seg.count/gradeStat.stdCount)*100)?string("##.#")}%</td>
          [/#list]
       </tr>
       <tr align="center">
          <td align="left">实考人数</td>
          <td>${gradeStat.stdCount}</td>
          <td align="center">最高得分数</td>
          <td>[#if gradeStat.heighest?exists]${gradeStat.heighest?string("##.#")}[/#if]</td>
          <td align="center">最低得分数</td>
          <td colspan="2">[#if gradeStat.lowest?exists]${gradeStat.lowest?if_exists?string("##.#")}[/#if]</td>
       </tr>
       <tr>
           <td colspan="9">
           [@b.div style="margin-top:5px;text-align:left;"]
                 二、综合分析:<br>
          1.命题分析（就试卷的难易程度、覆盖面及试卷类型适宜情况等进行分析）<br>
          2.学生考试结果（就教师的教学方法、手段、内容及学生对课程理解、掌握等方面进行分析）<br>
          3.措施与方法（肯定有效措施与方法，寻找不足及其原因，提出改进意见）
           [/@]
           </td>
       </tr>
       <tr>
        <td colspan="9">
        <table border="0" cellpadding="0" cellspacing="0" width="95%" style="border-color:white">
          <tr valign="top">
            <td height="380" style="border-color:white" id="contentValue">${(Parameters["contentValue"])?default("")}</td>
          </tr>
        </table>
          [@b.div style="text-align:right;"]授课老师签名:<U>[#list 1..15 as  i]&nbsp;[/#list]</U>&nbsp;&nbsp;[/@]
          [@b.div style="text-align:right;"]日期:<U>[#list 1..15 as  i]&nbsp;[/#list]</U>&nbsp;&nbsp;[/@]
        </td>
       </tr>
     </table>
     [/#list]
[#--
     [#if courseStat.gradeSegStats.size()==0]
      <table width="95%" align="center">
        <tr><td>
      [@b.div style="width:95%;text-align:left;margin-top:20px;color:red;"]
        ${teachTask.semester.schoolYear}学年${teachTask.semester.name}学期,${teachTask.teachClass.name!}教学班,${teachTask.course.name!}课程没有成绩记录!
      [/@]
      </td></tr></table>
     [/#if] 
--]
     <table align="center" width="95%" border='0' style="font-size:15px;">
     <tr>
       <td>
       [#--
       <td  colspan="${2+segStat.scoreSegments?size}">
       --]
          [@b.div style="text-align:right;"]院系部主任签名:<U>[#list 1..15 as  i]&nbsp;[/#list]</U>&nbsp;&nbsp;[/@]
          [@b.div style="text-align:right;"]日期:<U>[#list 1..15 as  i]&nbsp;[/#list]</U>&nbsp;&nbsp;[/@]
       </td>
     </tr>
     <tr>
       <td >
       [@b.div style="margin-top:5px;"]
                  注:<br>
        &nbsp;&nbsp;1.课程考核试卷分析须按班级分析，然后汇总写出该课程考核试卷的综合分析。<br>
        &nbsp;&nbsp;2.考试结束后随试卷一同交院系保存。<br>
        &nbsp;&nbsp;&nbsp;&nbsp;(如不够书写，请另附纸)<br>
       [/@]
      </td>
     </tr>
    </table>
[/#list]
</div>
<form method="post" action="" name="actionForm">
  <input type="hidden" name="lesson.ids" value="${Parameters['lesson.ids']?default('')}"/>
</form>
[#--
[@b.form action="" theme="list"]
  [@b.textarea label="多个学号" name="codes" rows="10" cols="80" required="true"/]
  [@b.formfoot]
      [@b.submit value="action.submit"/]
  [/@]
[/@]
--]
[@b.foot/]