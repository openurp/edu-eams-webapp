[#ftl]
[@b.head/]
[@b.toolbar title="学生基本信息"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="stdPersonSearchForm" action="!search" target="stdPersonlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="stdPerson.code;人员编码"/]
      [@b.textfields names="stdPerson.name;姓名"/]
      <input type="hidden" name="orderBy" value="stdPerson.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="stdPersonlist" href="!search?orderBy=stdPerson.code"/]
    </td>
  </tr>
</table>
[@b.foot/]