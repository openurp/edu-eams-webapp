[#ftl]
[@b.head/]
[@b.toolbar title="学科目录"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="disciplineCatalogSearchForm" action="!search" target="disciplineCataloglist" title="ui.searchForm" theme="search"]
      [@b.textfields names="disciplineCatalog.code;代码"/]
      [@b.textfields names="disciplineCatalog.name;名称"/]
      <input type="hidden" name="orderBy" value="disciplineCatalog.name"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="disciplineCataloglist" href="!search?orderBy=disciplineCatalog.code"/]
    </td>
  </tr>
</table>
[@b.foot/]