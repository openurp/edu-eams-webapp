[#ftl]
[@b.head/]
[@b.toolbar title="学生类别"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="stdTypeSearchForm" action="!search" target="stdTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="stdType.code;代码"/]
      [@b.textfields names="stdType.name;名称"/]
      [@b.textfields names="stdType.labelType;标签类型"/]
      <input type="hidden" name="orderBy" value="stdType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="stdTypelist" href="!search?orderBy=stdType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]