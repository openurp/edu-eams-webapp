<#include "/template/head.ftl"/>
<body>
    <table id="bar"></table>
    <table class="frameTable_title">
        <tr>
            <td style="width:50px"/>
                <font color="blue"><@text name="action.advancedQuery"/></font>
            </td>
            <td>|</td>
        <form name="actionForm" target="pageIframe" method="post" action="" >
            <#include "/template/time/semester.ftl"/>
            <input type="hidden" name="task.semester.id" value="${semester.id}"/>
        </tr>
    </table>
    <table width="100%" class="frameTable">
        <tr>
            <td valign="top" width="200px" class="frameTable_view">
                <#include "searchForm.ftl"/>
            </td>
        </form>
            <td valign="top">
                <iframe src="#" id="pageIframe" name="pageIframe" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" height="100%" width="100%"></iframe>
            </td>
        </tr>
    <table>
    <script>
        var bar = new ToolBar("bar", "任务课程批量百分比设置", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addBlankItem();
        
        var form = document.actionForm;
        
        function search() {
            form.action = "courseGradePercent.action?method=search";
            form.target = "pageIframe";
            form.submit();
        }
        
        search();
    </script>
</body>
<#include "/template/foot.ftl"/>