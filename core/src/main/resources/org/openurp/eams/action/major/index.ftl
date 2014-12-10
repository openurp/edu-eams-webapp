[#ftl]
[@b.head/]
[@b.toolbar title="专业"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="majorSearchForm" action="!search" target="majorlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="major.code;代码"/]
      [@b.textfields names="major.name;名称"/]
      [@b.textfields names="major.project.name;项目名称"/]
      [@b.textfields names="major.category.name;学科门类"/]
      <input type="hidden" name="orderBy" value="major.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="majorlist" href="!search?orderBy=major.code"/]
    </td>
  </tr>
</table>
[@b.foot/]