[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="examTypeSearchForm" action="!search" target="examTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="examType.code;代码"/]
      [@b.textfields names="examType.name;名称"/]
      <input type="hidden" name="orderBy" value="examType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="examTypelist" href="!search?orderBy=examType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]