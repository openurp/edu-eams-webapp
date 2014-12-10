[#ftl]
[@b.head/]
[@b.toolbar title="学生学籍状态"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="stdStatusSearchForm" action="!search" target="stdStatuslist" title="ui.searchForm" theme="search"]
      [@b.textfields names="stdStatus.code;代码"/]
      [@b.textfields names="stdStatus.name;名称"/]
      <input type="hidden" name="orderBy" value="stdStatus.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="stdStatuslist" href="!search?orderBy=stdStatus.code"/]
    </td>
  </tr>
</table>
[@b.foot/]