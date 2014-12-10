[#ftl]
[@b.head/]
[@b.toolbar title="学生分类标签"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="stdLabelSearchForm" action="!search" target="stdLabellist" title="ui.searchForm" theme="search"]
      [@b.textfields names="stdLabel.code;代码"/]
      [@b.textfields names="stdLabel.name;名称"/]
      [@b.textfields names="stdLabel.labelType;标签类型"/]
      <input type="hidden" name="orderBy" value="stdLabel.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="stdLabellist" href="!search?orderBy=stdLabel.code"/]
    </td>
  </tr>
</table>
[@b.foot/]