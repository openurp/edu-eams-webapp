[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="examStatusSearchForm" action="!search" target="examStatuslist" title="ui.searchForm" theme="search"]
      [@b.textfields names="examStatus.code;代码"/]
      [@b.textfields names="examStatus.name;名称"/]
      <input type="hidden" name="orderBy" value="examStatus.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="examStatuslist" href="!search?orderBy=examStatus.code"/]
    </td>
  </tr>
</table>
[@b.foot/]