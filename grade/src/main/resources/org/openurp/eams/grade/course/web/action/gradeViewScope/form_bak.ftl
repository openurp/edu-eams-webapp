<#include "/templates/head.ftl"/>
 <script language="JavaScript" type="text/JavaScript" src="scripts/validator.js"></script>
 <body> 

 <#assign labInfo>注册提示管理</#assign>
 <#include "/templates/back.ftl"/>
    <table width="90%" align="center" class="formTable">
     <form action="" name="actionForm" method="post" >
       <@searchParams/>
	   <tr class="darkColumn">
	     <td align="center" colspan="8">检测评教完成管理</td>
	   </tr>	
	   <tr>
	     <td class="title"colspan=4 align="center">
	         <table  bordercolor="#006CB2" class="formTable" cellpadding="0" cellspacing="0">
	          <tr>
	           <td><br>
	            <div align="center">学生类别列表</div>&nbsp;
	            <select name="stdType" MULTIPLE size="10" onDblClick="JavaScript:moveSelectedOption(this.form['stdType'], this.form['choosedStdType'])" style="width:120px" style="background-color:#CCCCCC">
	 			<#list stdTypes?if_exists as stdType>
	  				<option value="${stdType.id}"><@i18nName stdType/></option>
		        </#list>
	            </select><br>
	           </td>
	           <td align="center" valign="middle">
	            <br><br>
	            &nbsp;<input OnClick="JavaScript:moveSelectedOption(this.form['stdType'], this.form['choosedStdType'])" type="button" value="&gt;&gt;" class="buttonStyle" style="width:35px;"/> &nbsp;
	            <br><br>
	            &nbsp;<input OnClick="JavaScript:moveSelectedOption(this.form['choosedStdType'], this.form['stdType'])" type="button" value="&lt;&lt;" class="buttonStyle" style="width:35px;"> &nbsp;
	            <br>
	           </td>
	           
	           <td align="center" class="normalTextStyle">
	            <div align="center">需提醒类别列表</div>&nbsp;
	            <select name="choosedStdType" MULTIPLE size="10" style="width:120px;" onDblClick="JavaScript:moveSelectedOption(this.form['choosedStdType'], this.form['stdType'])" style="background-color:#CCCCCC">
	           		<#list (scope.stdTypes)?if_exists as stdType>
	  				<option value="${stdType.id}"><@i18nName stdType/></option>
			        </#list> 
	            </select>&nbsp;
	           </td> 
	        </tr>
	     </table>
	     </td>
	     
	     <td class="title" colspan=4 align="center">
	         <table  bordercolor="#006CB2" class="formTable" cellpadding="0" cellspacing="0">
	          <tr>
	           <td><br>
	            <div align="center">入学年份列表</div>&nbsp;
	            <select name="enrollYear" MULTIPLE size="10" onDblClick="JavaScript:moveSelectedOption(this.form['enrollYear'], this.form['choosedEnrollYear'])" style="width:120px" style="background-color:#CCCCCC">
	 			<#list enrollYears?if_exists as enrollYear>
	  				<option value="${enrollYear}">${enrollYear}</option>
		        </#list>
	            </select><br>
	           </td>
	           <td align="center" valign="middle">
	            <br><br>
	            &nbsp;<input OnClick="JavaScript:moveSelectedOption(this.form['enrollYear'], this.form['choosedEnrollYear'])" type="button" value="&gt;&gt;" class="buttonStyle" style="width:35px;"/> &nbsp;
	            <br><br>
	            &nbsp;<input OnClick="JavaScript:moveSelectedOption(this.form['choosedEnrollYear'], this.form['enrollYear'])" type="button" value="&lt;&lt;" class="buttonStyle" style="width:35px;"> &nbsp;
	            <br>
	           </td>
	           
	           <td align="center" class="normalTextStyle">
	            <div align="center">需提醒年份列表</div>&nbsp;
	            <select name="choosedEnrollYear" MULTIPLE size="10" style="width:120px;" onDblClick="JavaScript:moveSelectedOption(this.form['choosedEnrollYear'], this.form['enrollYear'])" style="background-color:#CCCCCC">
	           		<#list (enrollYearList)?if_exists as enrollYear>
	  				<option value="${enrollYear}">${enrollYear}</option>
			        </#list> 
	            </select>&nbsp;
	           </td> 
	        </tr>
	     </table>
	     </td>
	   </tr>
	   <tr align="center">
	     <td colspan=1 class="title" id="f_status" align="center">是否检查评教:</td>
	     <td colspan=7 align="left"><@htm.radio2 name="scope.checkEvaluation" value=(scope.checkEvaluation)?default(true)/></td>
	   </tr>
	   <tr class="darkColumn" align="center">
	     <td colspan="8">
		   <button onClick="doAction(this.form)"> <@bean.message key="system.button.submit"/></button>
           <input type="hidden" name="choosedStdTypeIds"/> 
           <input type="hidden" name="scope.enrollYears"/> 
           <input type="hidden" name="scope.id"value="${(scope.id)?default('')}"/>
	       <input type="reset" name="reset" value="<@bean.message key="system.button.reset"/>" class="buttonStyle"/>
	     </td>
	   </tr>
	   </table>
     </td>
   </tr>
   </form>
   </table>
 <script language="javascript"/>
    function doAction(form){
	    form['choosedStdTypeIds'].value = getAllOptionValue(form.choosedStdType); 
	    form['scope.enrollYears'].value = getAllOptionValue(form.choosedEnrollYear); 
	    form.action="gradeViewScope.do?method=save";
        form.submit();
    }
 </script>
</body>
<#include "/templates/foot.ftl"/>