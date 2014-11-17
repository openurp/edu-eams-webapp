<table width="100%" onkeypress="dwr.util.onReturn(event, actionForm)">
    <tr>
        <td colspan="2" align="left" valign="bottom">
            <img src="${base}/static/images/action/info.gif" align="top"/><B><@text name="ui.searchForm"/></B>
        </td>
    </tr>
    <tr>
        <td colspan="2" style="font-size:0px">
            <img src="${base}/static/images/action/keyline.gif" height="2" width="100%" align="top"/>
        </td>
    </tr>
    <tr>
        <td width="40%">专业代码:</td>
        <td><input name="major.code" type="text" value="" style="width:100px" maxlength="20"/></td>
    </tr>
    <tr>
        <td>专业名称:</td>
        <td><input name="major.name" type="text" value="" style="width:100px" maxlength="50"/></td>
    </tr>
    <#include "/template/major3Select.ftl"/>
    <@majorSelect id="" projectId="major.project.id" educationId="education.id" departId="major.department.id"/>
    <tr>
        <td class="infoTitle">是否双专:</td>
        <td>
            <select name="major.project.minor" style="width:100px">
                <option value="0">否</option>
                <option value="1">是</option>
            </select>
        </td>
    </tr>
    <tr>
        <td><@text name="common.status"/>:</td>
        <td>
            <select name="major.enabled" style="width:100px;">
                <option value="1" selected><@text name="common.enabled"/></option>
                <option value="0"><@text name="common.disabled"/></option>
            </select>
       </td>
    </tr>
    
    <tr align="center" height="50px">
        <td colspan="2">
            <button onclick="search()" class="buttonStyle" style="width:60px"><@text name="action.query"/></button>
        </td>
    </tr>
</table>
<script>
    $("education").onchange = function () {
        ;
    }
    $("department").onchange = function () {
        ;
    }
</script>
