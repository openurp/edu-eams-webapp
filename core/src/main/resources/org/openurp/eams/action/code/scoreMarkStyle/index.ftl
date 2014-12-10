[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="scoreMarkStyleSearchForm" action="!search" target="scoreMarkStylelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="scoreMarkStyle.code;代码"/]
      [@b.textfields names="scoreMarkStyle.name;名称"/]
      <input type="hidden" name="orderBy" value="scoreMarkStyle.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="scoreMarkStylelist" href="!search?orderBy=scoreMarkStyle.code"/]
    </td>
  </tr>
</table>
[@b.foot/]