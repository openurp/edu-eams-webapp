<#include "/template/head.ftl"/>
<BODY LEFTMARGIN="0" TOPMARGIN="0">
  <table id="gradeListBar" width="100%"></table>
  <@table.table width="100%" sortable="true" id="listTable">
   <@table.thead>
     <@table.selectAllTd id="stdId"/>
     <@table.sortTd width="10%" name="attr.stdNo" id="gradeInfo.std.code"/>
     <@table.sortTd width="8%" name="attr.personName" id="gradeInfo.std.name"/>
       <@table.sortTd width="8%" name="course.stdType" id="gradeInfo.std.stdType.name"/>
      <@table.sortTd  width="15%" id="gradeInfo.std.department.name" name="department"/>
      <@table.sortTd width="15%" name="entity.major" id="gradeInfo.std.major.name"/>
      <@table.sortTd width="15%" name="entity.direction"  id="gradeInfo.std.direction.name"/>
     <td width="8%">未通过的学分</td>
   </@>
   <@table.tbody datas=gradeInfos?if_exists;gradeInfo>
    <@table.selectTd id="stdId" value=gradeInfo.std.id/>
    <td><a href="javascript:gradeInfo(${gradeInfo.std.id})" alt="查看详细信息">${gradeInfo.std.code}</a></td>
    <td><@i18nName gradeInfo.std/></td>
      <td><@i18nName gradeInfo.std.stdType/></td>
   	<td><@i18nName gradeInfo.std.department/></td>
   	<td><@i18nName gradeInfo.std.major/></td>
   	<td><@i18nName gradeInfo.std.direction?if_exists /></td>
   	<td>${gradeInfo.tolCredit?default(0)}</td>
   </@>
</@>
  <form name="gradeListForm" method="post" action="gradeFailCreditStats.action?method=info" ></form>
  <script>
    var bar = new ToolBar("gradeListBar","成绩查询结果",null,true,true);
    bar.addItem("查看","gradeInfo()");
    
    function gradeInfo(stdId){
      document.gradeListForm.target = "_self";
      document.gradeListForm.action = "gradeFailCreditStats.action?method=info";
      if (null == stdId) {
        submitId(document.gradeListForm,"stdId",false);
      } else {
        addInput(document.gradeListForm,"stdId",stdId);
        document.gradeListForm.submit();
      }
    }
  </script>
</body>
<#include "/template/foot.ftl"/>
