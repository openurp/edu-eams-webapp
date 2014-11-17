<#include "/template/head.ftl"/>
<script language="JavaScript" type="text/JavaScript" src="static/scripts/common/Validator.js"></script>
<body>
    <table id="bar1"></table>
    <table class="formTable" width="100%">
    <form method="post" name="actionForm" action="" >
        <input type="hidden" name="courseIds" value="${Parameters["courseIds"]}"/>
        <input type="hidden" name="isPercentSetting" value="${Parameters["isPercentSetting"]}"/>
        <input type="hidden" name="task.semester.id" value="${Parameters["task.semester.id"]}"/>
        <input type="hidden" name="params" value="<#list Parameters?keys as key><#if key != "method">&${key}=${Parameters[key]}</#if></#list>"/>
        <tr>
            <td class="title" width="20%">总评记录方式:</td>
            <td width="30%" colspan="2"><@htm.i18nSelect datas=markStyles?sort_by(["code"]) selected="" name="GAMarkStyleId" style="width:150px"/></td>
            <td class="title" width="20%">成绩精确度:</td>
            <td width="30%" colspan="2">
                <select name="percision" style="width:150px">
                    <option value="0">不保留小数</option>
                    <option value="1">保留一位小数</option>
                </select>
            </td>
        </tr>
        <tr>
        <#list canInputGradeTypes as gradeType>
            <td class="title" id="f_percent${gradeType.id}">${gradeType.name}:</td>
            <td style="border-right-width:0px" width="8%"><input type="text" name="percent${gradeType.id}" value="0" maxlength="3" style="width:50px"/></td>
            <td style="border-left-width:0px"><@htm.i18nSelect datas=markStyles?sort_by(["code"]) selected="" name="markStyle${gradeType.id}" style="width:100px"/></td>
                <#if gradeType_index % 2 == 1 && gradeType_has_next>
        </tr>
        <tr>
                </#if>
        </#list>
        <#if canInputGradeTypes?size % 2 != 0>
            <td class="title"></td>
            <td colspan="2"></td>
        </#if>
        </tr>
    </form>
    </table>
    <table id="bar2"></table>
    <@table.table id="courseGradePercent" width="100%">
        <@table.thead>
            <@table.td name="attr.courseNo" width="10%"/>
            <@table.td name="attr.courseName" width="15%"/>
            <@table.td name="attr.credit" width="6%"/>
            <@table.td name="对应课程序号"/>
            <#if Parameters["isPercentSetting"]?default("0") == "1">
                <@table.td name="百分比设置情况" width="40%"/>
            </#if>
        </@>
        <@table.tbody datas=courseMap?keys;key>
            <#assign tasks = courseMap[key][0]/>
            <#assign course = tasks?first.course/>
            <td>${course.code}</td>
            <td>${course.name}</td>
            <td>${course.credits}</td>
            <td><#list tasks as task>${task.seqNo}<#if task_has_next>, </#if></#list></td>
            <#if Parameters["isPercentSetting"]?default("0") == "1">
            <#assign gradeStates = courseMap[course.id?string][1]/>
            <td><#list gradeStates as gradeState><#if (gradeStates?size > 1)>${gradeState_index + 1}:</#if><#list gradeState.gradeTypeStates?sort_by(["gradeType", "code"]) as gradeTypeState>${gradeTypeState.gradeType.name}占${gradeTypeState.percent * 100}%<#if gradeTypeState_has_next>，</#if></#list><#if gradeState_has_next><br></#if></#list></td>
            </#if>
        </@>
    </@>
    <script>
        var bar1 = new ToolBar("bar1", "批量设置课程百分比", null, true, true);
        bar1.setMessage('<@getMessage/>');
        bar1.addItem("保存", "save()");
        bar1.addBackOrClose("<@text name="action.back"/>", "<@text name="action.close"/>");
        
        var bar2 = new ToolBar("bar2", "所选课程列表", null, true, true);
        bar2.setMessage('<@getMessage/>');
        bar2.addBlankItem();
        
        var form = document.actionForm;
        
        function save() {
            var a_fields = {
                <#list canInputGradeTypes as gradeType>
                'percent${gradeType.id}':{'l':'${gradeType.name}', 'r':true, 't':'f_percent${gradeType.id}', 'f':'unsigned'}<#if gradeType_has_next>,</#if>
                </#list>
            };
            var v = new validator(form, a_fields, null);
            if (v.exec()) {
                if (<#list canInputGradeTypes as gradeType>Number(form["percent${gradeType.id}"].value) > 100 <#if gradeType_has_next> || </#if></#list>) {
                    alert("所设成绩类型的百分比数值不能\n超过100％。");
                    return
                }
                if (<#list canInputGradeTypes as gradeType>Number(form["percent${gradeType.id}"].value)<#if gradeType_has_next> + </#if></#list> != 100) {
                    alert("所设成绩类型的百分比总和不等于\n100％，请检查。");
                    return
                }
                if (confirm(<#if Parameters["hasGrade"]?default("0") == "1">autoLineFeed("所选择的课程中有或部分任务有学生成绩了，修改后可能会对学生的成绩总评产生影响，如果所占百分比设为 0 后，将删除这个成绩类型的成绩。", 56) + "\n" + </#if>autoLineFeed("确定要如此设置所选课程对应教学任务的成绩百分比吗？", 56))) {
                    form.action = "courseGradePercent.action?method=saveBatchPercent";
                    form.submit();
                }
            }
        }
    </script>
</body>
<#include "/template/foot.ftl"/>