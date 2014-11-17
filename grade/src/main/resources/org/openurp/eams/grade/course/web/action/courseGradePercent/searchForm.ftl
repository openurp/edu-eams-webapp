<table width="100%" border="0">
   <tr>
        <td class="infoTitle" align="left" valign="bottom" style="font-weight: bold"><img src="${base}/static/images/action/info.gif" align="top"/>&nbsp;详细查询(模糊输入)</td>
    </tr>
    <tr>
        <td colspan="8" style="font-size:0px"><img src="${base}/static/images/action/keyline.gif" height="2" width="100%" align="top"/></td>
    </tr>
</table>
<table class="searchTable" width="100%">
    <tr>
        <td width="45%">课程代码:</td>
        <td><input type="text" name="course.code" value="" maxlength="10" style="width:100px"/></td>
    </tr>
    <tr>
        <td width="45%">课程名称:</td>
        <td><input type="text" name="course.name" value="" maxlength="30" style="width:100px"/></td>
    </tr>
    <tr>
        <td width="45%">课程百分比:</td>
        <td>
            <select name="isPercentSetting" style="width:100px">
                <option value="0">未设置</option>
                <option value="1">已设置</option>
            </select>
        </td>
    </tr>
    <tr>
        <td width="45%">考试方式:</td>
        <td><@htm.i18nSelect datas=examModes selected="" name="course.ext.examMode.id" style="width:100px"></@></td>
    </tr>
    <tr>
        <td colspan="2" height="50px" align="center"><button onclick="search()">查询</button></td>
    </tr>
    <tr height="200px">
        <td colspan="2"></td>
    </tr>
</table>