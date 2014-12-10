[#ftl]
[@b.head/]
[@b.toolbar title="教师职称等级"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="teacherTitleLevelSearchForm" action="!search" target="teacherTitleLevellist" title="ui.searchForm" theme="search"]
      [@b.textfields names="teacherTitleLevel.code;代码"/]
      [@b.textfields names="teacherTitleLevel.name;名称"/]
      <input type="hidden" name="orderBy" value="teacherTitleLevel.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="teacherTitleLevellist" href="!search?orderBy=teacherTitleLevel.code"/]
    </td>
  </tr>
</table>
[@b.foot/]