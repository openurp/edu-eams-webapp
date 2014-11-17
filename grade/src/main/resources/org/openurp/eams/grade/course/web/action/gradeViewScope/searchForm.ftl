<table class="searchTable" onkeypress="DWRUtil.onReturn(event, searchSetting)" width="100%">
    <tr>
        <td colspan="2" class="scopeTitle" align="left" valign="bottom" style="font-weight:bold"><img src="${base}/static/images/action/info.gif" align="top"/> 详细查询</td>
    </tr>
    <tr>
      <td colspan="2" style="font-size:0px">
          <img src="${base}/static/images/action/keyline.gif" height="2" width="100%" align="top"/>
      </td>
    </tr>
    <form name="searchForm" action="" target="contentListFrame" method="post" >
    <tr>
        <td width="40%">是否检查评教:</td>
        <td>
            <select name="scope.checkEvaluation" style="width:100%">
                <option value="1">检查</option>
                <option value="0">不检查</option>
            </select>
        </td>
    </tr>
    <tr height="50px">
        <td align="center" colspan="2"><button onclick="search()"><@text name="action.query"/></button></td>
    </tr>
    </form>
</table>
