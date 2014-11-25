[#ftl]
[@b.head/]
[@b.toolbar title="学生成绩"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="indexSearchForm" action="!info" target="indexInfo" title="ui.searchForm" theme="search"]
      [@b.textfields names="lesson.no;课程序号"/]  
      <input type="hidden" name="orderBy" value="courseGrade.lesson.no"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="indexInfo" href="!info?orderBy=courseGrade.lesson.id"/]
    </td>
  </tr>
</table>
[@b.foot/]