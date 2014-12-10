[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="degreeSearchForm" action="!search" target="degreelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="degree.code;代码"/]
      [@b.textfields names="degree.name;名称"/]
      <input type="hidden" name="orderBy" value="degree.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="degreelist" href="!search?orderBy=degree.code"/]
    </td>
  </tr>
</table>
[@b.foot/]