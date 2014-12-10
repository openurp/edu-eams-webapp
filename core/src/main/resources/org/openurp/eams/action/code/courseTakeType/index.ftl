[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseTakeTypeSearchForm" action="!search" target="courseTakeTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="courseTakeType.code;代码"/]
      [@b.textfields names="courseTakeType.name;名称"/]
      <input type="hidden" name="orderBy" value="courseTakeType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courseTakeTypelist" href="!search?orderBy=courseTakeType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]