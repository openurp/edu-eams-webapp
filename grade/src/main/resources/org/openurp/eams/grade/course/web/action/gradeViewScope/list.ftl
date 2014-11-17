<#include "/template/head.ftl"/>
<BODY>
    <table id="bar" width="100%"></table>
    <#macro toValues datas length>
        <#local dataValues><#list datas as data>${data.name}<#if data_has_next>，</#if></#list></#local>
        <#if dataValues?exists && "" != dataValues>
            <#if (dataValues?length > length)>
            <td title="${dataValues}">${dataValues[0..length - 3]}...</td>
            <#else>
            <td>${dataValues}</td>
            </#if>
        <#else>
            <td></td>
        </#if>
    </#macro>
    <@table.table width="100%" id="listTable" sortable="true">
        <@table.thead>
            <@table.selectAllTd id="scopeId"/>
            <@table.td text="教学项目范围" width="20%"/>
            <@table.td text="学历层次范围" width="20%"/>
            <@table.td text="学生类别范围" width="20%"/>
            <@table.sortTd text="入学年份范围" id="scope.enrollYears" width="20%"/>
            <@table.sortTd text="是否检查评教" id="scope.opened"/>
        </@>
        <@table.tbody datas=scopes;scope>
            <@table.selectTd id="scopeId" value=scope.id/>
            <@toValues datas=scope.projects length=40/>
            <@toValues datas=scope.educations length=40/>
            <@toValues datas=scope.stdTypes length=40/>
            <td>${(scope.enrollYears?replace(",", ", "))?if_exists}</td>
            <td>${scope.checkEvaluation?string("检查", "不检查")}</td>
        </@>
    </@>
    <form method="post" action="" name="actionForm" >
        <input type="hidden" name="scopeId" value=""/>
        <input type="hidden" name="scopeIds" value=""/>
        <#assign filterKeys = ["method"]/>
        <input type="hidden" name="params" value="<#list Parameters?keys as key><#if !filterKeys?seq_contains(key)>&${key}=${Parameters[key]?if_exists}</#if></#list>"/>
    </form>
    <script>
        var bar = new ToolBar("bar", "成绩检查评教设置项列表", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addItem("添加", "add()");
        bar.addItem("修改", "edit()");
        bar.addItem("删除", "remove()");
        
        var form = document.actionForm;
        
        function initData() {
            form["scopeId"].value = "";
            form["scopeIds"].value = "";
        }
        
        initData();
    
        function add(){
            initData();
            form.action = "gradeViewScope.action?method=edit";
            form.target = "_self";
            form.submit();
        }
        
        function edit(){
            initData();
            var scopeId = getSelectId("scopeId");
            if (isEmpty(scopeId) || isMultiId(scopeId)) {
                alert("请选择一条要操作的记录。");
                return;
            }
            form.action = "gradeViewScope.action?method=edit";
            form["scopeId"].value = scopeId;
            form.target = "_self";
            form.submit();
        }
        
        function remove(){
            var scopeIds = getSelectIds("scopeId");
            if (isEmpty(scopeIds)) {
                alert("请选择要操作的记录。");
                return;
            }
            if (confirm("确认删除选定的设置吗？")) {
                form.action = "gradeViewScope.action?method=remove";
                form["scopeIds"].value = scopeIds;
                form.target = "_self";
                form.submit();
            }
        }
        
        parent.toResize(document.body);
    </script>
</body>
<#include "/template/foot.ftl"/>