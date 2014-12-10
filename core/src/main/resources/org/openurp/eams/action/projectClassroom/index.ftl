[#ftl]
[@b.head/]
[@b.toolbar title="项目教室配置"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="projectClassroomSearchForm" action="!search" target="projectClassroomlist" title="ui.searchForm" theme="search"]
      
      [@b.textfields names="projectClassroom.project.name;项目名称"/]
      <input type="hidden" name="orderBy" value="projectClassroom.project.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="projectClassroomlist" href="!search?orderBy=projectClassroom.id"/]
    </td>
  </tr>
</table>
[@b.foot/]