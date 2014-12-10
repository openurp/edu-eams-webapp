[#ftl]
[@b.head/]
[@b.toolbar title="学生分类标签信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${stdLabel.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${stdLabel.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">标签类型</td>
    <td class="content">${stdLabel.labelType}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${stdLabel.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${stdLabel.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${stdLabel.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${stdLabel.remark!}</td>
  </tr>
</table>

[@b.foot/]