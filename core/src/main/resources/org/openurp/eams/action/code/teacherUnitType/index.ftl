[#ftl]
[@b.head/]
[@b.toolbar title="外聘教师单位类别"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="teacherUnitTypeSearchForm" action="!search" target="teacherUnitTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="teacherUnitType.code;代码"/]
      [@b.textfields names="teacherUnitType.name;名称"/]
      <input type="hidden" name="orderBy" value="teacherUnitType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="teacherUnitTypelist" href="!search?orderBy=teacherUnitType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]