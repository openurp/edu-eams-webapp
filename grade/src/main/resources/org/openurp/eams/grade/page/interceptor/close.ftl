[#ftl]
[@b.head/]
[@b.toolbar title="${b.text('grade.teachClassInput')}"]
	 bar.addClose("${b.text("action.close")}");
[/@]
<link href="${base}/static/styles/grade-input-panel.css" rel="stylesheet" type="text/css">
<table class="gradePanel" align="center" style="text-align:center;width:95%">
    <tr>
        <td align="center">
            <table style="padding:2%;width:100%">
                <tr>
                    <td colspan="2"><h2>未开放成绩录入</h2></td>
                </tr>
                <tr>
                    <td width="100px">课程序号:</td>
                    <td>${lesson.no}</td>
                </tr>
                <tr>
                    <td>课程名称:</td>
                    <td>${lesson.course.name}</td>
                </tr>
            </table>
        </td>
    </tr>
</table>
[@b.foot/]