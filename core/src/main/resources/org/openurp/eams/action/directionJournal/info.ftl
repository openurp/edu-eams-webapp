[#ftl]
[@b.head/]
[@b.toolbar title="专业方向建设过程信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">专业方向过程id</td>
    <td class="content">${directionJournal.id}</td>
  </tr>
  <tr>
    <td class="title" width="20%">专业方向</td>
    <td class="content">${directionJournal.direction.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">培养层次</td>
    <td class="content">${directionJournal.education.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">部门</td>
    <td class="content">${directionJournal.depart.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${directionJournal.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${directionJournal.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${directionJournal.remark!}</td>
  </tr>
</table>

[@b.foot/]