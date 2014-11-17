[#ftl]
[@b.head/]
[@b.toolbar title="${b.text('grade.teachClassInput')}"]
	 bar.addClose("${b.text("action.close")}");
[/@]
<link href="${base}/static/styles/grade-input-panel.css" rel="stylesheet" type="text/css">

<link href="${base}/static/css/tab.css" rel="stylesheet" type="text/css">
    <table class="gradePanel" align="center" style="text-align:center;width:95%">
        <tr>
            <td align="center">
                <table style="padding:2%;width:100%">
                    <tr>
                        <td colspan="3"><h2>请勿尝试录入非您执教的课程成绩</h2></td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
[@b.foot/]