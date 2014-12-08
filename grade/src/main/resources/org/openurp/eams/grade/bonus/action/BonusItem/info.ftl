[#ftl]
[@b.head/]
<style>
ol {
margin-top: 0;
margin-bottom: 0px;
}
</style>
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
    <td class="title" width="20%">排除加分课程列表</td>
    <td class="content">
      <ol>
        [#list bonusItem.courses as course]
          <li>${course.code}&nbsp${course.name}&nbsp${course.credits!}</li>
        [/#list]
      </ol>
  </tr>
  <tr>
    <td class="title" width="20%">开始学期</td>
    <td class="content" >${(bonusItem.beginTime.schoolYear)!}-${(bonusItem.beginTime.name)!}</td>
  </tr>
  <tr>
    <td class="title" width="20%">结束学期</td>
    <td class="content" >[#if bonusItem.endTime??]${bonusItem.endTime.schoolYear}-${bonusItem.endTime.name}[/#if]</td>
  </tr>
</table>

[@b.foot/]