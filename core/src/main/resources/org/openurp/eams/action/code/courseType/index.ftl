[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseTypeSearchForm" action="!search" target="courseTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="courseType.code;代码"/]
      [@b.textfields names="courseType.name;名称"/]
      <input type="hidden" name="orderBy" value="courseType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courseTypelist" href="!search?orderBy=courseType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]