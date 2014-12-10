[#ftl]
[@b.head/]
[@b.toolbar title="项目配置属性信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
   <tr>
    <td class="title" width="20%">项目名称</td>
    <td class="content">${projectProperty.config.project.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">项目配置id</td>
    <td class="content">${projectProperty.config.id!}</td>
  </tr>
   <tr>
    <td class="title" width="20%">配置项名称</td>
    <td class="content">${projectProperty.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">配置项值</td>
    <td class="content">${projectProperty.value!}</td>
  </tr>
</table>

[@b.foot/]