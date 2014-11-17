<#include "/template/head.ftl"/>
<body>
    <table id="bar"></table>
    <@table.table id="courseGradePercent" width="100%" sortable="true">
        <@table.thead>
            <@table.selectAllTd id="courseId"/>
            <@table.sortTd name="attr.courseNo" id="course.code"/>
            <@table.sortTd name="attr.courseName" id="course.name"/>
            <@table.sortTd name="attr.credit" id="course.credits" width="7%"/>
            <@table.sortTd name="对应课程序号" id="course.credits" width="18%"/>
            <#if Parameters["isPercentSetting"]?default("0") == "1">
                <@table.td name="百分比设置情况" width="35%"/>
            </#if>
            <@table.td name="是否有成绩" width="10%"/>
        </@>
        <@table.tbody datas=courses;course>
            <@table.selectTd id="courseId" value=course.id/>
            <td>${course.code}</td>
            <td>${course.name}</td>
            <td>${course.credits}</td>
            <td><#list courseMap[course.id?string][0] as task>${task.seqNo}<#if task_has_next>, </#if></#list></td>
            <#if Parameters["isPercentSetting"]?default("0") == "1">
            <#assign gradeStates = courseMap[course.id?string][1]/>
            <td><#list gradeStates as gradeState><#if (gradeStates?size > 1)>${gradeState_index + 1}:</#if><#list gradeState.gradeTypeStates?sort_by(["gradeType", "code"]) as gradeTypeState>${gradeTypeState.gradeType.name}占${gradeTypeState.percent * 100}%<#if gradeTypeState_has_next>，</#if></#list><#if gradeState_has_next><br></#if></#list></td>
            </#if>
            <td id="hasGrade${course.id}">${courseMap[course.id?string][2]?exists?string("有", "无")}</td>
        </@>
    </@>
    <@htm.actionForm name="actionForm" action="courseGradePercent.action" entity="course" >
    </@>
    <script>
        var bar = new ToolBar("bar", "任务课程列表<font color=\"red\">（有成绩无法设置百分比）</font>", null, true, true);
        bar.setMessage('<@getMessage/>');
        bar.addItem("设置百分比", "editBatchPercent()", "update.gif");
        
        function editBatchPercent() {
            var courseIds = getSelectIds("courseId");
            if (null == courseIds || "" == courseIds) {
                alert("请选择要操作的记录。");
                return;
            }
            var courseIdArray = courseIds.split(",");
            for (var i = 0; i < courseIdArray.length; i++) {
                if ($("hasGrade" + courseIdArray[i]).innerHTML == "有") {
                    if (confirm(autoLineFeed("所选择的课程中有或部分任务有学生成绩了，修改后可能会对学生的成绩总评产生影响，如果所占百分比设为 0 后，将删除这个成绩类型的成绩。", 48) + "\n\n要继续吗？")) {
                        addInput(form, "hasGrade", "1", "hidden");
                        break;
                    } else {
                        return;
                    }
                }
            }
            multiAction("editBatchPercent");
        }
    </script>
</body>
<#include "/template/foot.ftl"/>