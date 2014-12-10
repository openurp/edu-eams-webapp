[#ftl]
[@b.head/]
[@b.toolbar title="学籍状态日志信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${studentJournal.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${studentJournal.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">英文名</td>
    <td class="content">${studentJournal.enName!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">生效时间</td>
    <td class="content" >${studentJournal.beginOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">失效时间</td>
    <td class="content" >${studentJournal.endOn!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">备注</td>
    <td class="content">${studentJournal.remark!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">专业</td>
    <td class="content">${studentJournal.major.name!}</td>
  </tr>
</table>

[@b.foot/]