<#include "/template/head.ftl"/>
 <BODY LEFTMARGIN="0" TOPMARGIN="0" >
  <table id="gradeListBar" width="100%"> </table>
  <#include "../courseGradeListTable.ftl"/>
  <form name="gradeListForm" method="post" action="gradeTransfer.action?method=transfer" ></form>
  <script>
    var bar = new ToolBar("gradeListBar","双专业成绩查询结果",null,true,true);
    bar.setMessage('<@getMessage/>');
    bar.addItem("转移到一专业","transfer()");
    bar.addPrint("<@text name="action.print"/>");
    
    function transfer(){
      var params = getInputParams(parent.document.stdSearch,null,false);
	  addParamsInput(document.gradeListForm,params);
      submitId(gradeListForm,"courseGradeId",true,"${b.url('transfer!transfer')}" ,"确定转移双转业成绩吗?\n如果转移操作失误,可以在[学生成绩]管理中更改过来.");
    }
    function gradeInfo(id) {
      //下面的是添加的
      var form = document.gradeListForm;
      form.action="${b.url('stdGrade!info')}";
      form.target="_self";
      if(null==id){
        submitId(form,"courseGradeId",false);
      }else{
        addInput(form,"courseGradeId",id);
        document.gradeListForm.submit();
      }
    }
    
    parent.toResize(document.body);
  </script>
 </body>
<#include "/template/foot.ftl"/>
