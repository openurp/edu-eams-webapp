[#ftl]
[@b.head/]
[@b.toolbar title="项目配置"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="projectConfigSearchForm" action="!search" target="projectConfiglist" title="ui.searchForm" theme="search"]
      
      [@b.textfields names="projectConfig.project.name;项目名称"/]
      <input type="hidden" name="orderBy" value="projectConfig.project.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="projectConfiglist" href="!search?orderBy=projectConfig.id"/]
    </td>
  </tr>
</table>
[@b.foot/]