[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="gradeTypeSearchForm" action="!search" target="gradeTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="gradeType.code;代码"/]
      [@b.textfields names="gradeType.name;名称"/]
      <input type="hidden" name="orderBy" value="gradeType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="gradeTypelist" href="!search?orderBy=gradeType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]