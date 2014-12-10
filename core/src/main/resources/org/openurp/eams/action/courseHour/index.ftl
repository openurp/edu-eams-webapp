[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseHourSearchForm" action="!search" target="courseHourlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="courseHour.id;id"/]
      <input type="hidden" name="orderBy" value="courseHour.id"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courseHourlist" href="!search?orderBy=courseHour.id"/]
    </td>
  </tr>
</table>
[@b.foot/]