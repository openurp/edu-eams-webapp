[#ftl]
[@b.head/]
[@b.toolbar title="项目基础代码配置"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="projectCodeSearchForm" action="!search" target="projectCodelist" title="ui.searchForm" theme="search"]
      
      [@b.textfields names="projectCode.project.name;项目名称"/]
      <input type="hidden" name="orderBy" value="projectCode.project.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="projectCodelist" href="!search?orderBy=projectCode.id"/]
    </td>
  </tr>
</table>
[@b.foot/]