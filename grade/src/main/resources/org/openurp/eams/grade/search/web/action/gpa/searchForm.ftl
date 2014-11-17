 <table width="100%">
	    <tr>
	      <td class="infoTitle" align="left" valign="bottom">
	       <img src="${base}/static/images/action/info.gif" align="top"/>
	          <B><@text name="baseinfo.searchStudent"/></B>
	      </td>
	    </tr>
	    <tr>
	      <td colspan="8" style="font-size:0px">
	          <img src="${base}/static/images/action/keyline.gif" height="2" width="100%" align="top"/>
	      </td>
	   </tr>	
  </table>
  <table width='100%' class="searchTable" onkeypress="dwr.util.onReturn(event, search)">
    <input type="hidden" name="pageNo" value="1" />
    	<tr>
	     <td class="infoTitle" width="40%"><@text name="attr.stdNo"/>:</td>
	     <td>
	      <input type="text" name="stdGpa.std.code" value="${Parameters['std.code']?if_exists}" maxlength="32" style="width:100%"/>
	     </td>
		</tr>
    	<tr>
	     <td class="infoTitle"><@text name="attr.personName"/>:</td>
	     <td>
	      <input type="text" name="stdGpa.std.name" value="${Parameters['std.name']?if_exists}" maxlength="20" style="width:100%"/>
	     </td>
		</tr>
	   <tr>
	     <td class="infoTitle">年级:</td>
	     <td><input type="text" name="stdGpa.std.grade" id='std.grade' style="width:100%;" maxlength="7"/></td>
	   </tr>
           <#include "/template/major3SelectForGrade.ftl"/>
           <@majorSelect id="" projectId="stdGpa.std.project.id" educationId="stdGpa.std.education.id" departId="stdGpa.std.department.id" majorId="stdGpa.std.major.id" directionId="stdGpa.std.direction.id" stdTypeId="stdGpa.std.stdType.id"/>
    	<tr>
	     <td class="infoTitle"><@text name="common.adminClass"/>:</td>
	     <td>
	      <input type="text" name="stdGpa.std.adminclass.name" value="" style="width:100%;" maxlength="20"/>
         </td>
        </tr>
    	<tr>
	     <td class="infoTitle">是否在籍:</td>
	     <td>
	      <select name="stdGpa.std.active" style="width:100%">
	        <option value="">全部</option>
	        <option value="1" selected>有效</option>
	        <option value="0">无效</option>
	      </select>
         </td>
        </tr>
        
        <tr>
	     <td class="infoTitle">是否在校:</td>
	     <td>
	      <select name="stdGpa.std.inSchool" style="width:100%">
	        <option value="">全部</option>
	        <option value="1" selected>有效</option>
	        <option value="0">无效</option>
	      </select>
         </td>
        </tr>
        
    	<tr>
	     <td class="infoTitle">是否双专:</td>
	     <td>
	      <select name="stdGpa.minor" style="width:100%">
	        <option value="0">否</option>
	        <option value="1">是</option>
	      </select>
         </td>
        </tr>
	    <tr align="center">
	     <td colspan="2">
		     <button style="width:60px" onClick="search(1)"><@text name="action.query"/></button>
	     </td>
	    </tr>
  </table>
