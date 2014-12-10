[#ftl]
[@b.head/]
[@b.toolbar title="学生分类标签类型"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="stdLabelTypeSearchForm" action="!search" target="stdLabelTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="stdLabelType.code;代码"/]
      [@b.textfields names="stdLabelType.name;名称"/]
      <input type="hidden" name="orderBy" value="stdLabelType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="stdLabelTypelist" href="!search?orderBy=stdLabelType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]