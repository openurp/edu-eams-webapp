[#ftl]
[@b.head/]
[@b.toolbar title="学位信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${adminclass.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${adminclass.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">简称</td>
    <td class="content">${adminclass.abbreviation!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${adminclass.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${adminclass.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${adminclass.remark!}</td>
  </tr>
</table>

[@b.foot/]