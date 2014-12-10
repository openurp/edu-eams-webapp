[#ftl]
[@b.head/]
[@b.toolbar title="项目"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="projectSearchForm" action="!search" target="projectlist" title="ui.searchForm" theme="search"]
      
      [@b.textfields names="project.name;名称"/]
      <input type="hidden" name="orderBy" value="project.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="projectlist" href="!search?orderBy=project.id"/]
    </td>
  </tr>
</table>
[@b.foot/]