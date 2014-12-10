[#ftl]
[@b.head/]
[@b.toolbar title="专业信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${major.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${major.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">项目名称</td>
    <td class="content">${major.project.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">学科门类</td>
    <td class="content">${major.category.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${major.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${major.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${major.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${major.remark!}</td>
  </tr>
</table>

[@b.foot/]