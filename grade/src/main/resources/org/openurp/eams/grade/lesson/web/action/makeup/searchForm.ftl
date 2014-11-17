	   <table  width="100%">
	    <tr>
	      <td  class="infoTitle" align="left" valign="bottom">
	       <img src="static/images/action/info.gif" align="top"/>
	          <B>${b.text("action.advancedQuery.like")}</B>
	      </td>
	    </tr>
	    <tr>
	      <td  colspan="8" style="font-size:0px">
	          <img src="static/images/action/keyline.gif" height="2" width="100%" align="top">
	      </td>
	   </tr>	
	  </table>
	  <table class="searchTable"  onkeypress="dwr.util.onReturn(event, search)">
	    <tr>
	     <td class="infoTitle"><@text name="attr.courseNo"/>:</td>
	     <td><input name="examTake.task.course.code" type="text" value="" style="width:60px" maxlength="32"/></td>
	    </tr>
	    <tr>
	     <td class="infoTitle"><@text name="attr.courseName"/>:</td>
	     <td><input type="text" name="examTake.task.course.name" value="" style="width:100px" maxlength="20"/></td>
	    </tr>
	    <tr>
	     <td class="infoTitle"><@text name="attr.teachDepart"/>:</td>
	     <td>
		     <select name="examTake.task.teachDepart.id" value="" style="width:100px">
		     	<option value=""><@text name="common.all"/></option>
		     	<#list sort_byI18nName(teachDepartList) as teachDepart>
		     	<option value=${teachDepart.id}><@i18nName teachDepart/></option>
		     	</#list>-->
		     </select>
	     </td>
	    </tr>
	    <#--<tr>
	     <td class="infoTitle"><@text name="entity.teacher"/>:</td>
	     <td><input type="text" name="activity.teacher.name" value="" style="width:100px" maxlength="20"/>
	     </td>
	    </tr>
	    <tr>
	     <td class="infoTitle"><@text name="std.grade"/>:</td>
	     <td><input type="text" name="activity.task.teachClass.grade" value="" maxlength="7" style="width:60px"></td>
	    </tr>-->
	    <tr align="center">
	     <td colspan="2">
		     <button onClick="search();" accesskey="Q"  style="width:60px">
		       <@text name="action.query"/>(<U>Q</U>)
		     </button>		    
	     </td>
	    </tr>
	  </table>
