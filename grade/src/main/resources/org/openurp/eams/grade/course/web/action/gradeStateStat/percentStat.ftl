<#include "/template/head.ftl"/>
<body onload="parent.$('error').innerHTML = '';parent.$('error').style.display = 'none'">
    <table id="bar"></table>
    <@table.table id="gradePercentResultId" width="100%">
        <@table.thead>
            <@table.td text="录入百分比设定情况"/>
            <@table.td text="对应记录数"/>
        </@>
        <@table.tbody datas=results?keys; key>
            <#assign gradeTypeArray = key?string?split("_")/>
            <#if key == "0_0_0_0_0">
            <td width="70%" style="color:red">未设定百分比</td>
            <#else>
            <td width="70%"><#if gradeTypeArray[0] != "0">平时成绩 ${gradeTypeArray[0]}％ </#if><#if gradeTypeArray[0] != "0" && gradeTypeArray[1] != "0">，</#if><#if gradeTypeArray[1] != "0">期中成绩 ${gradeTypeArray[1]}％ </#if><#if gradeTypeArray[1] != "0" && gradeTypeArray[2] != "0">，</#if><#if gradeTypeArray[2] != "0">期末成绩 ${gradeTypeArray[2]}％ </#if><#if gradeTypeArray[2] != "0" && gradeTypeArray[3] != "0">，</#if><#if gradeTypeArray[3] != "0">补考成绩 ${gradeTypeArray[3]}％ </#if><#if gradeTypeArray[3] != "0" && gradeTypeArray[4] != "0">，</#if><#if gradeTypeArray[4] != "0">缓考成绩 ${gradeTypeArray[4]}％</#if></td>
            </#if>
            <td>${results[key?string]}</td>
        </@>
    </@>
    <script>
        var bar = new ToolBar("bar", "百分比状态统计结果", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addPrint();
    </script>
</body>
<#include "/template/foot.ftl"/>
