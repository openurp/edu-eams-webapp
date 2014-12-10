[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="examGradeSearchForm" action="!search" target="examGradelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="examGrade.id;id"/]
      <input type="hidden" name="orderBy" value="examGrade.id"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="examGradelist" href="!search?orderBy=examGrade.id"/]
    </td>
  </tr>
</table>
[@b.foot/]