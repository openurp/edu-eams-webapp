<#include "/template/head.ftl"/>
<body>
  <#--
        <#include "/components/initAspectSelectData.ftl"/>  
     -->
 <table id="myBar"></table>
 <table class="frameTable_title">
    <tr>
       <td  style="width:50px">
          <font color="blue"><@text name="action.advancedQuery"/></font>
       </td>
       <td>|</td>
    <form name="searchForm" target="reportFrame" method="post" action="multiStdGpa.action?method=index" >
   </tr>
  </table>
  <table width="100%" border="0" height="100%" class="frameTable">
   <tr>
     <td style="width:20%" valign="top" class="frameTable_view">
		<#include "/components/adminClass3SelectTable.ftl"/>
     </td>
    </form>
    <td valign="top">
	     <iframe src="#" id="reportFrame" name="reportFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" height="100%" width="100%"></iframe>
     </td>
    </tr>
  <table>
	<script>
	    var bar=new ToolBar("myBar","行政班学生绩点表",null,true,true);
	    bar.addHelp("<@text name="action.help"/>");
	    
	    var action="multiStdGpa.action";
	    function search(pageNo,pageSize,orderBy){
	       var form = document.searchForm;
		   form.action = action+"?method=adminClassList";
		   form.target="reportFrame";
	       goToPage(form,pageNo,pageSize,orderBy);
	    }
	    searchClass=search;
	    search();
	</script>
</body>
<#include "/template/foot.ftl"/> 
