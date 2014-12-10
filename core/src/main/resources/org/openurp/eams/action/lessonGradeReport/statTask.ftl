[#ftl]
[#macro i18nName(object)]
  [#if object.enName??]${object.enName}[#else]${object.name}[/#if]
 [/#macro]
[@b.head/]
[@b.toolbar title="成绩分段统计"]
  bar.addPrint();
    bar.addClose();
[/@]

<style type="text/css">
.statTable{
  width:60%;
  align:center;
}
</style>
[#macro getTeacherNames teachers][#list teachers as teacher][#if teacher_index gt 0]、[/#if]${teacher.name!}[/#list][/#macro]
[#list courseStats as courseStat]
     [#assign teachTask=courseStat.lesson]
   <div align='center'><h3>${teachTask.project.school.name}成绩分段统计表</h3></div>
   <div align='center'>${teachTask.semester.schoolYear!}学年 ${(teachTask.semester.name)?if_exists?replace("0","第")}学期</div><br>   
   <table class="gridtable" style="width:95%" align="center">
    <tr>
            <td>开课院(系、部):[@i18nName teachTask.teachDepart/]</td>
      <td colSpan='2'>主讲教师:[@getTeacherNames teachTask.teachers?if_exists/]</td>
    </tr>
    <tr>
      <td>课程序号:${teachTask.no?if_exists}</td>
        <td width='40%'>课程名称:${teachTask.course.name!}[#if teachTask.subCourse??]<sup>${teachTask.subCourse.name}</sup>[/#if]</td>
      <td align='left'>课程类别:${teachTask.courseType.name!}</td>
    </tr>
   </table>
   <table class="gridtable" style="width:95%" align="center">
      <tr>
        <td>分段依据:</td>
      [#list courseStat.scoreSegments as seg]
       <td>${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
      [/#list]
      </tr>
  </table>
  
    <p></p>
  <table class="gridtable" style="width:95%" align="center">
      <thead class="gridhead">
        <tr>
          <th></th>
          [#list courseStat.gradeSegStats as gradeStat]
            [#assign gradeTypeName]${gradeStat.gradeType.name!}[/#assign]
            <th colspan="2">
            ${gradeTypeName}<br>
            <span style="font-weight:normal">人数(${gradeStat.stdCount})平均分:${(gradeStat.average?string("##.#"))?default('')} 最高分:${(gradeStat.heighest?string("##.#"))?default('')} 最低分:${(gradeStat.lowest?string("##.#"))?default('')}</span>
            </th>
            [/#list]
        </tr>
        <tr>
            <th>成绩段</th>
            [#list courseStat.gradeSegStats as gradeStat]
            <th>人数</th>
            <th>占总人数的比例</th>
            [/#list]
        </tr>
      </thead>
      <tbody>
    [#list courseStat.scoreSegments as seg]
        <tr class="${(seg_index%2==0)?string('griddata-even','griddata-odd')}">
          <td>${seg.min?string("##.#")}-${seg.max?string("##.#")}</td>
          [#list courseStat.gradeSegStats as gradeStat]
            [#assign meSeg = gradeStat.scoreSegments[seg_index] /]
            <td>${meSeg.count}</td>
            <td>[#if gradeStat.stdCount != 0]${((meSeg.count/gradeStat.stdCount)*100)?string("##.#")}%[/#if]</td>
          [/#list]
        </tr>
      [/#list]
      </tbody>
  </table>
    [#if courseStat_has_next]
    <div style='PAGE-BREAK-AFTER: always'></div>
    [/#if]
[/#list]
[@b.foot/]