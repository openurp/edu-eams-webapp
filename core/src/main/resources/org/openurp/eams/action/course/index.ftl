[#ftl]
[@b.head/]
[@b.toolbar title="学位"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="courseSearchForm" action="!search" target="courselist" title="ui.searchForm" theme="search"]
      [@b.textfields names="course.code;代码"/]
      [@b.textfields names="course.name;名称"/]
      [@b.textfields names="course.department.name;院系"/]
      <input type="hidden" name="orderBy" value="course.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="courselist" href="!search?orderBy=course.code"/]
    </td>
  </tr>
</table>
[@b.foot/]