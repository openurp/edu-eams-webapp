[#ftl]
[@b.head/]
[@b.toolbar title="专业建设过程信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">专业</td>
    <td class="content">${majorJournal.major.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">学科门类</td>
    <td class="content">${majorJournal.category.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">培养层次</td>
    <td class="content">${majorJournal.education.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">部门</td>
    <td class="content">${majorJournal.depart.name!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">修读年限</td>
    <td class="content">${majorJournal.duration!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">教育部代码</td>
    <td class="content" >${majorJournal.disciplineCode!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${majorJournal.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${majorJournal.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${majorJournal.remark!}</td>
  </tr>
</table>

[@b.foot/]