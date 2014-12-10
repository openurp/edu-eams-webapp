[#ftl]
[@b.head/]
[@b.toolbar title="学习形式"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="studyTypeSearchForm" action="!search" target="studyTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="studyType.code;代码"/]
      [@b.textfields names="studyType.name;名称"/]
      <input type="hidden" name="orderBy" value="studyType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="studyTypelist" href="!search?orderBy=studyType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]