[#ftl]
[@b.head/]
[@b.toolbar title="教师类型"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="teacherTypeSearchForm" action="!search" target="teacherTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="teacherType.code;代码"/]
      [@b.textfields names="teacherType.name;名称"/]
      [@b.select name="teacherType.parttime" label="是否兼职" items={'true':'是','false':'否'} empty="..." style="width:100px"/]
      <input type="hidden" name="orderBy" value="teacherType.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="teacherTypelist" href="!search?orderBy=teacherType.code"/]
    </td>
  </tr>
</table>
[@b.foot/]