[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseGradeSearchForm" action="!search" target="courseGradelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="courseGrade.id;id"/]
      <input type="hidden" name="orderBy" value="courseGrade.id"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courseGradelist" href="!search?orderBy=courseGrade.id"/]
    </td>
  </tr>
</table>
[@b.foot/]