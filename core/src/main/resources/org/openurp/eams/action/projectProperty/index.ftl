[#ftl]
[@b.head/]
[@b.toolbar title="项目配置属性"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="projectPropertySearchForm" action="!search" target="projectPropertylist" title="ui.searchForm" theme="search"]
      [@b.textfields names="projectProperty.config.project.name;项目名称"/]
      [@b.textfields names="projectProperty.config.id;项目配置id"/]
      <input type="hidden" name="orderBy" value="projectProperty.config.id"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="projectPropertylist" href="!search?orderBy=projectProperty.id"/]
    </td>
  </tr>
</table>
[@b.foot/]