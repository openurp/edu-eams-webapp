<table width="100%">
	    <tr>
	      <td  align="left" valign="bottom">
	       <img src="static/images/action/info.gif" align="top"/>
	          <B>详细查询(模糊输入)</B>
	      </td>
	    </tr>
	    <tr>
	      <td  colspan="8" style="font-size:0px">
	          <img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/>
	      </td>
	   </tr>
	  </table>
    <table width="100%" class="searchTable" onkeypress="dwr.util.onReturn(event, search)">
    <form name="stdSearch" method="post" action="" >
        <input type="hidden" name="pageNo" value="1"/>
    	<tr>
	     <td width="40%"><@text name="attr.stdNo"/>:</td>
	     <td>
	      <input type="text" name="gradeInfo.std.code" maxlength="32" size="10" value="${Parameters['student.code']?if_exists}" style="width:100px;"/>
	     </td>
		</tr>
    	<tr>
	     <td><@text name="attr.personName"/>:</td>
	     <td>
	      <input type="text" name="gradeInfo.std.name" maxlength="20" size="10" value="${Parameters['student.name']?if_exists}" style="width:100px;"/>
	     </td>
		</tr>
		<tr>
	     <td>学分>=:</td>
	     <td>
	      <input type="text" name="lowerCredit" maxlength="20" size="10" style="width:100px;"/>
	     </td>
		</tr>
	    <tr>
	     <td>学分<=:</td>
	     <td>
	      <input type="text" name="upperCredit" maxlength="20" size="10" style="width:100px;"/>
	     </td>
		</tr>
	   <tr>
	     <td>就读年级:</td>
	     <td><input type="text" name="gradeInfo.std.grade" maxlength="7" id='student.grade' style="width:100px;"></td>
	   </tr>
       <tr> 
	     <td><@text name="entity.studentType"/>:</td>
	     <td>
	          <select id="stdTypeOfMajor" name="gradeInfo.std.stdType.id" style="width:100px;">
	            <option value="${Parameters['student.stdType.id']?if_exists}"><@text name="filed.choose"/></option>
	          </select>	 
         </td>
		</tr>
    	<tr>
	     <td><@text name="common.college"/>:</td>
	     <td>
           <select id="department" name="gradeInfo.std.department.id" style="width:100px;">
         	  <option value=""><@text name="filed.choose"/>...</option>
           </select>
         </td>
        </tr> 
	   <tr>
	     <td><@text name="entity.major"/>:</td>
	     <td>
           <select id="major" name="gradeInfo.std.major.id" style="width:100px;">
         	  <option value=""><@text name="filed.choose"/>...</option>
           </select>
         </td>
        </tr>
	   <tr>
	     <td><@text name="entity.direction"/>:</td>
	     <td>
           <select id="direction" name="gradeInfo.std.direction.id" style="width:100px;">
         	  <option value=""><@text name="filed.choose"/>...</option>
           </select>
         </td>
        </tr>
	    <tr align="center" height="50px">
	     <td colspan="2">
		     <button  onClick="search(1)" style="width:60px"><@text name="action.query"/></button>
	     </td>
	    </tr>
    </form>
  </table>
<#include "/template/stdTypeDepart3Select.ftl"/>
<script src='${base}/dwr/interface/semesterDao.js'></script>
<script src='${base}/static/scripts/common/SemesterSelect.js'></script>
