[#ftl]
[@b.head/]
[@b.toolbar title="学科"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="disciplineSearchForm" action="!search" target="disciplinelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="discipline.code;代码"/]
      [@b.textfields names="discipline.name;名称"/]
      <input type="hidden" name="orderBy" value="discipline.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="disciplinelist" href="!search?orderBy=discipline.code"/]
    </td>
  </tr>
</table>
[@b.foot/]