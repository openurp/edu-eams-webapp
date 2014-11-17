[#ftl]
[@b.head/]
[@b.toolbar title="加分项信息"]
  bar.addBack("${b.text("action.back")}");
[/@]
<table class="infoTable">
  <tr>
    <td class="title" width="20%">代码</td>
    <td class="content">${bonusItem.code}</td>
  </tr>
  <tr>
    <td class="title" width="20%">名称</td>
    <td class="content">${bonusItem.name}</td>
  </tr>
  <tr>
    <td class="title" width="20%">年级</td>
    <td class="content">${bonusItem.grade!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">学生类别标签</td>
    <td class="content">${(bonusItem.stdLabel.name)!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">开始学期</td>
    <td class="content" >${(bonusItem.beginTime.name)!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">结束学期</td>
    <td class="content" >${(bonusItem.endTime.name)!}</td>
  </tr>
</table>

[@b.foot/]