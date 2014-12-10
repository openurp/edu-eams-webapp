[#ftl]
[@b.head/]
[@b.toolbar title="教师在职状态"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="teacherStateSearchForm" action="!search" target="teacherStatelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="teacherState.code;代码"/]
      [@b.textfields names="teacherState.name;名称"/]
      <input type="hidden" name="orderBy" value="teacherState.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="teacherStatelist" href="!search?orderBy=teacherState.code"/]
    </td>
  </tr>
</table>
[@b.foot/]