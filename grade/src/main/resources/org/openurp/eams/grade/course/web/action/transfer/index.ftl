<#include "/template/head.ftl"/>
 <body >  
 <table id="gradeBar"></table>
  <table width="100%" border="0" height="100%" class="frameTable">
  <tr >
     <td style="width:20%" valign="top" class="frameTable_view">
     <#assign otherParams>
		<input name="courseGrade.project.minor" value="1" type="hidden"/>
     </#assign>
		<#include "../stdGradeSearch/searchForm.ftl"/>
     </td>
     <td valign="top">
	     <iframe  src="#" id="gradeFrame" name="gradeFrame" 
	      marginwidth="0" marginheight="0"
	      scrolling="no" 	 frameborder="0"  height="100%" width="100%">
	     </iframe>
     </td>
    </tr>
  <table>
<script>
    var bar=new ToolBar("gradeBar","成绩转移管理",null,true,true);
    bar.addBlankItem();
    
    function search(pageNo,pageSize,orderBy){
       var form = document.stdSearch;
	   form.action = "${b.url('transfer!search')}";
	   form.target="gradeFrame";
       goToPage(form,pageNo,pageSize,orderBy);
    }
    
    function toResize(obj) {
        var defaultHeight = parent.parent.document.getElementById("mainTable").offsetHeight - 90;
        $("gradeFrame").style.height = obj.offsetHeight < defaultHeight ? defaultHeight : obj.scrollHeight;
    }
        
    search();
</script> 
 </body>   
<#include "/template/foot.ftl"/> 
