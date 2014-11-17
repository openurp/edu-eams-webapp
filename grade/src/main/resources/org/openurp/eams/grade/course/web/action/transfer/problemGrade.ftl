<#include "/template/head.ftl"/>
<body>
    <table id="bar" width="100%"></table>
    <h2 style="color:red">有问题的原因是：学生没有对应第二专业，或者第二专业过多。</h2>
    <@table.table width="100%" sortable="true" id="listTable">
        <@table.thead>
            <@table.sortTd name="attr.stdNo" id="courseGrade.std.code,courseGrade.id"/>
            <@table.sortTd width="8%" name="attr.personName" id="courseGrade.std.name,courseGrade.id"/>
            <@table.sortTd width="7%" name="attr.taskNo" id="courseGrade.taskSeqNo,courseGrade.id"/>
            <@table.sortTd width="7%" name="attr.courseNo" id="courseGrade.course.code,courseGrade.id"/>
            <@table.sortTd width="12%" name="entity.course" id="courseGrade.course.name,courseGrade.id"/>
            <@table.sortTd name="entity.courseType" id="courseGrade.courseType.name,courseGrade.id"/>
            <@table.sortTd width="5%"text="成绩" id="courseGrade.score,courseGrade.id"/>
            <@table.sortTd width="4%" name="attr.credit" id="courseGrade.course.credits,courseGrade.id"/>
            <@table.sortTd width="4%" text="绩点" id="courseGrade.gp,courseGrade.id"/>
            <@table.sortTd width="10%" text="学年学期" id="courseGrade.semester.beginOn,courseGrade.id"/>
        </@>
        <@table.tbody datas=untransferGrades;grade>
            <td>${grade.std.code}</td>
            <td><@i18nName grade.std/></td>
            <td>${grade.taskSeqNo?if_exists}</td>
            <td>${grade.course.code}</td>
            <td><@i18nName grade.course?if_exists/></td>
            <td><@i18nName grade.courseType?if_exists/></td>
            <td title="<#list grade.examGrades as eg>${eg.gradeType.name} ${eg.scoreText!}<#if eg_has_next>,</#if></#list>"<#if !grade.passed> style="color:red"</#if>>${grade.getScoreText()}</td>
            <td>${(grade.course.credits)?if_exists}</td>
            <td<#if !grade.passed> style="color:red"</#if>>${(grade.gp?string("#.##"))?if_exists}</td>
            <td>${grade.semester.schoolYear} ${grade.semester.name}</td>
        </@>
    </@>
    <form method="post" action="" name="actionForm" >
        <input type="hidden" name="ruleId" value=""/>
        <input type="hidden" name="ruleIds" value=""/>
        <#assign filterKeys = ["method", "params"]/>
        <input type="hidden" name="params" value="<#list Parameters?keys as key><#if !filterKeys?seq_contains(key)>&${key}=${Parameters[key]?if_exists}</#if></#list>"/>
    </form>
    <script>
        var bar = new ToolBar("bar", "有问题的成绩记录（共 ${untransferGrades?size} 条）", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addBlankItem();
        
        parent.toResize(document.body);
    </script>
</body>
<#include "/template/foot.ftl"/>
