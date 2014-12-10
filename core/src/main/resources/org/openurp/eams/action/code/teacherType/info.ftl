[#ftl]
[@b.head/]
[@b.toolbar title="教师类型信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${teacherType.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${teacherType.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${teacherType.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">是否兼职</td>
    <td class="content">${(teacherType.parttime?string("是","否"))!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${teacherType.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${teacherType.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${teacherType.remark!}</td>
  </tr>
</table>

[@b.foot/]