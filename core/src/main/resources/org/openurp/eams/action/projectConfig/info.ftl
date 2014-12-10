[#ftl]
[@b.head/]
[@b.toolbar title="项目配置信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
   <tr>
    <td class="title" width="20%">项目名称</td>
    <td class="content">${projectConfig.project.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">项目配置id</td>
    <td class="content">${projectConfig.id!}</td>
  </tr>
</table>

[@b.foot/]