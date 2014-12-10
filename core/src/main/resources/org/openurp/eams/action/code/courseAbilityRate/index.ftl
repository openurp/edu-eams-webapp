[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseAbilityRateSearchForm" action="!search" target="courseAbilityRatelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="courseAbilityRate.code;代码"/]
      [@b.textfields names="courseAbilityRate.name;名称"/]
      <input type="hidden" name="orderBy" value="courseAbilityRate.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courseAbilityRatelist" href="!search?orderBy=courseAbilityRate.code"/]
    </td>
  </tr>
</table>
[@b.foot/]