[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="examModeSearchForm" action="!search" target="examModelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="examMode.code;代码"/]
      [@b.textfields names="examMode.name;名称"/]
      <input type="hidden" name="orderBy" value="examMode.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="examModelist" href="!search?orderBy=examMode.code"/]
    </td>
  </tr>
</table>
[@b.foot/]