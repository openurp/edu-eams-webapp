[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="adminclassSearchForm" action="!search" target="adminclasslist" title="ui.searchForm" theme="search"]
      [@b.textfields names="adminclass.code;代码"/]
      [@b.textfields names="adminclass.name;名称"/]
      <input type="hidden" name="orderBy" value="adminclass.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="adminclasslist" href="!search?orderBy=adminclass.code"/]
    </td>
  </tr>
</table>
[@b.foot/]