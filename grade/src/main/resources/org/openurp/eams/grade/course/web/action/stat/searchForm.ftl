<table width="100%">
    <tr>
        <td colspan="2" style="font-weight:bold"><img src="static/images/action/info.gif" align="top"/>任务课程查询条件</td>
    </tr>
    <tr>
        <td colspan="2" style="font-size:0px"><img src="static/images/action/keyline.gif" height="2" width="100%" align="top"/></td>
    </tr>
    <tr>
        <td width="40%">课程代码：</td>
        <td><input type="text" name="task.course.code" value="" maxlength="30" style="width:100%"/></td>
    </tr>
    <tr>
        <td>课程名称：</td>
        <td><input type="text" name="task.course.name" value="" maxlength="50" style="width:100%"/></td>
    </tr>
    <tr>
        <td>项目：</td>
        <td>
            <select id="project" name="task.project.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>${b.text("entity.education")}：</td>
        <td>
            <select id="education"  name="task.course.education.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>学生类别：</td>
        <td>
            <select id="stdType"  name="task.teachClass.stdType.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>所在院系：</td>
        <td>
            <select id="department"  name="task.teachClass.depart.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>所属专业：</td>
        <td>
            <select id="major"  name="task.teachClass.major.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>方向：</td>
        <td>
            <select id="direction"  name="task.teachClass.direction.id" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>学年度：</td>
        <td>
            <select id="schoolYear"  name="task.semester.schoolYear" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>学期：</td>
        <td>
            <select id="term"  name="task.semester.name" style="width:100%;">
                <option value="">...</option>
            </select>
        </td>
    </tr>
    <tr height="50px">
        <td colspan="2" align="center"><button onclick="search()">查询</button></td>
    </tr>
</table>
