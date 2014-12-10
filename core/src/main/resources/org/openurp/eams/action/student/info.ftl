[#ftl]
[@b.head/]
[@b.toolbar title="学籍信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">学号</td>
    <td class="content">${student.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">姓名</td>
    <td class="content">${student.person.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${student.person.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">性别</td>
    <td class="content" >${(student.person.gender.name)!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">年级</td>
    <td class="content" >${student.grade!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">行政管理院系</td>
    <td class="content">${(student.department.name)!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">专业</td>
    <td class="content">${(student.major.name)!}</td>
  </tr>
</table>

[@b.foot/]