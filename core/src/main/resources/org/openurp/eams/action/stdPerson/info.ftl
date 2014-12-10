[#ftl]
[@b.head/]
[@b.toolbar title="学生基本信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">人员编码</td>
    <td class="content">${stdPerson.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">姓名</td>
    <td class="content">${stdPerson.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${stdPerson.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">性别</td>
    <td class="content" >${stdPerson.gender.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">身份证</td>
    <td class="content" >${stdPerson.idcard!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">国家地区</td>
    <td class="content">${stdPerson.country.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">民族</td>
    <td class="content">${stdPerson.nation.name!}</td>
  </tr>
</table>

[@b.foot/]