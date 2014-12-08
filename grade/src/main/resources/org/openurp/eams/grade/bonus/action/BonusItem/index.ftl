[#ftl]
[@b.head/]
[@b.toolbar title="加分项"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="bonusItemSearchForm" action="!search" target="bonusItemlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="bonusItem.code;代码"/]
      [@b.textfields names="bonusItem.name;名称"/]
      <input type="hidden" name="orderBy" value="bonusItem.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="bonusItemlist" href="!search?orderBy=bonusItem.code"/]
    </td>
  </tr>
</table>
[@b.foot/]