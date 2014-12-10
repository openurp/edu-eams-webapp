[#ftl]
[@b.head/]
[@b.toolbar title="导师类型"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="tutorTypeSearchForm" action="!search" target="tutorTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="tutorType.code;代码"/]
      [@b.textfields names="tutorType.name;名称"/]
      <input type="hidden" name="orderBy" value="tutorType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="tutorTypelist" href="!search?orderBy=tutorType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]