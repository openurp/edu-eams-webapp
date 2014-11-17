<#include "/template/head.ftl"/>
<BODY LEFTMARGIN="0" TOPMARGIN="0">
    <table id="myBar"></table>
    <table class="frameTable" width="100%">
        <tr>
            <td style="width:20%" class="frameTable_view"><#include "searchForm.ftl"/></td>
            <td valign="top">
                <iframe src="#" id="contentListFrame" name="contentListFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" height="100%" width="100%"></iframe>
            </td>
        </tr>
    </table>
    <script language="javascript">
        var bar = new ToolBar("myBar","成绩检查评教设置管理",null,true,true);
        bar.addBlankItem();
        
        var form = document.searchForm;
        
        function search(){
            form.action = "gradeViewScope.action?method=search";
            form.target = "contentListFrame";
            form.submit(); 
        }
        
        function toResize(obj) {
            var defaultHeight = parent.parent.document.getElementById("mainTable").offsetHeight - 60;
            $("contentListFrame").style.height = obj.offsetHeight < defaultHeight ? defaultHeight : obj.scrollHeight;
        }
        
        search();
    </script>
</body>
<#include "/template/foot.ftl"/>