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
          <input type="text" name="courseGrade.std.code" maxlength="32" size="10" value="${Parameters['student.code']?if_exists}" style="width:100%;"/>
         </td>
        </tr>
        <tr>
         <td><@text name="attr.personName"/>:</td>
         <td>
          <input type="text" name="courseGrade.std.name" maxlength="20" size="10" value="${Parameters['student.name']?if_exists}" style="width:100%;"/>
         </td>
        </tr>
       <tr>
         <td>就读年级:</td>
         <td><input type="text" name="courseGrade.std.grade" maxlength="7" id='student.grade' style="width:100%;"></td>
       </tr>
        <#assign projectNullable=true/>
        <#assign educationNullable=true/>
        <#assign departmentNullable=true/>
        <#assign studentTypeNullable=true/>
        <#assign majorNullable=true/>
        <#assign directionNullable=true/>
        <#assign yearNullable=true/>
        <#assign termNullable=true/>
        <#assign projectDefaultFirst=false/>
        <#include "/template/major3SelectForAll.ftl"/>
        <@majorSelect id="" isDisplayProject="1" projectId="courseGrade.project.id" year="year" term="term" educationId="courseGrade.education.id" departmentId="courseGrade.std.department.id" majorId="courseGrade.std.major.id" directionId="courseGrade.std.direction.id" stdTypeId="courseGrade.std.stdType.id"/>
        <tr>
         <td><@text name="attr.taskNo"/>:</td>
         <td>
          <input type="text" name="courseGrade.taskSeqNo" maxlength="32" value="${Parameters["courseGrade.taskSeqNo"]?if_exists}" style="width:100%;"/>
         </td>
        </tr>
        <tr>
         <td><@text name="attr.courseNo"/>:</td>
         <td>
          <input type="text" name="courseGrade.course.code" maxlength="32" value="${Parameters["courseGrade.course.code"]?if_exists}" style="width:100%;"/>
         </td>
        </tr>
        <tr>
         <td><@text name="attr.courseName"/>:</td>
         <td>
          <input type="text" name="courseGrade.course.name" maxlength="20" value="${Parameters["courseGrade.course.name"]?if_exists}" style="width:100%;"/>
         </td>
        </tr>
        <tr>
         <td><@text name="entity.courseType"/>:</td>
         <td>
          <input type="text" name="courseGrade.courseType.name" maxlength="20" value="${Parameters["courseGrade.courseType.name"]?if_exists}" style="width:100%;"/>
         </td>
        </tr>
        <tr>
         <td>状态:</td>
         <td>
           <select name="courseGrade.status" style="width:100%;">
             <option value="">全部</option>
             <option value="0"><@text name="action.new"/></option>
             <option value="1">录入确认</option>
             <option value="2">已发布</option>
          </select>
         </td>
        </tr>
        <tr>
         <td>分数范围:</td>
         <td><input name="scoreFrom" value="${Parameters["scoreFrom"]?if_exists}" maxlength="3" style="width:45%"/>-<input name="scoreTo" maxlength="3" value="${Parameters["scoreTo"]?if_exists}" style="width:45%"/></td>
        </tr>
       <tr>
         <td>是否通过:</td>
         <td>
         <select name="isPass" id="isPass" style="width:100%;">
            <option value="">全部</option>
            <option value="1">通过</option>
            <option value="0">未通过</option>
            <option value="3">一直未通过</option>
          </select>
         </td>
       </tr>
        ${otherParams?if_exists}
        <tr align="center" height="50px">
         <td colspan="2">
             <button  onClick="search(1)" style="width:60px"><@text name="action.query"/></button>
         </td>
        </tr>
    </form>
  </table>
