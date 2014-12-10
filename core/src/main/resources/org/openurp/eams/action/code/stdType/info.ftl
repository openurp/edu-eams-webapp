[#ftl]
[@b.head/]
[@b.toolbar title="学生类别信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${stdType.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${stdType.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">标签类型</td>
    <td class="content">${stdType.labelType}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${stdType.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${stdType.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${stdType.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${stdType.remark!}</td>
  </tr>
</table>

[@b.foot/]