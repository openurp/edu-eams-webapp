<#include "/template/head.ftl"/>
<body>
 <table id="myBar"></table>
 <script language="javascript">
 var bar = new ToolBar("myBar","请对以下课程进行评教",null,true,true);
 </script>
  <@table.table width="100%" id="listTable" sortable="true">
    <@table.thead>
       <@table.td text="课程代码"/>
       <@table.td text="课程名称"/>
       <@table.td text="学分"/>
       <@table.td text="评教入口"/>
    </@>
    <@table.tbody datas=courseTakeList;take>
       <td>${(take.task.course.code)?if_exists}</td>
       <td><a href="evaluateStd.do?method=loadQuestionnaire&evaluateId=${take.task.id}&evaluateState=evaluate&calendar.id=${take.task.calendar.id}">${(take.task.course.name)?if_exists}</a></td>
       <td>${(take.task.course.credits)?if_exists}</td>
       <td><a href="evaluateStd.do?method=loadQuestionnaire&evaluateId=${take.task.id}&evaluateState=evaluate&calendar.id=${take.task.calendar.id}">开始评教</a></td>
    </@>
   </@>
   <p>
   <font color="red"><strong>只有评教完成方能查看成绩。</strong></font>
   </p>
</body>
<#include "/template/foot.ftl"/>