[#ftl]
[@b.head/]
[@b.toolbar title="教师职称"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="teacherTitleSearchForm" action="!search" target="teacherTitlelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="teacherTitle.code;代码"/]
      [@b.textfields names="teacherTitle.name;名称"/]
      <input type="hidden" name="orderBy" value="teacherTitle.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="teacherTitlelist" href="!search?orderBy=teacherTitle.code"/]
    </td>
  </tr>
</table>
[@b.foot/]