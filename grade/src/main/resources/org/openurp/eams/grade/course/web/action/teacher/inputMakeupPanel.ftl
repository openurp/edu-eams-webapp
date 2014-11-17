[#ftl]
[#--补缓考成绩录入--]
<div class="dynamic-tab-pane-control tab-pane" id="tabPane3">
<form method="post" action="" name="actionForm3" id="actionForm3" target="_self">
    <input type="hidden" name="lessonId" value="${lesson.id}"/>
    <input type="hidden" name="gradeTypeIds" value="${MAKEUP.id},${DELAY_ID}"/>
        <script type="text/javascript">tp3 = new WebFXTabPane(document.getElementById("tabPane3"), false);</script>
        <div style="display: block;height:100px;padding:5px;font-size:14px;background-color:#E1ECFF;border-color:LightSkyBlue;" class="tab-page" id="tabPage3_1">
            <h2 class="tab" style="background-color:#E1ECFF;font-size:14px;width:100px;border-color:LightSkyBlue;"><b>${b.text('grade.makeupdelay')}</b></h2>
            <script type="text/javascript">tp3.addTabPage(document.getElementById("tabPage3_1"));</script>
            <table width="100%" height="20px" cellpadding="0" cellspacing="0">
                <tr>[#assign makeupGradeTypeState = gradeState.getState(MAKEUP)!/]
                    [@large_stateinfo status=(makeupGradeTypeState.status!0)/]
                    <td align="right">[#if makeupGradeTypeState?? && makeupGradeTypeState.inputedAt??]上次录入:${makeupGradeTypeState.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
                </tr>
            </table>
            <table align="center" width="100%" cellpadding="0" cellpadding="0">
              <tr valign="top">
                <td width="20%">
                    <table cellpadding="0" cellspacing="0" >
                        <tr>
                            [@gradeInfoHTML url="${b.url('/teach/grade/lesson/teacherReport!blank?makeup=1&lesson.id=' +lesson.id)}" onclick="" tdStyle1="" tdStyle2="text-align:left" caption="空白登分表" iconSize="25px" width1="" width2="100px"/]
                        </tr>
                    </table>
                </td>
                <td>
            <table align="center" style="font-size:14px;text-align:center" cellpadding="0" cellspacing="0">
                <tr height="35px">
                    [@gradeInfoHTML url="${b.url('teacher-makeup!info')}?lessonId=${lesson.id}" onclick="" tdStyle1="" tdStyle2="text-align:left" caption="查看" iconSize="25px" width1="" width2="40px"/]
                    [#if !(makeupGradeTypeState.confirmed)?default(false)]
                    [#if gradeInputSwitch.checkOpen()]
                    [@inputHTML url="#" tdStyle1="" tdStyle2="text-align:left" onclick="makeupInput()" caption="录入" iconSize="25px" width1="" width2="40px"/]
                    [@removeGradeHTML url="#" onclick="makeupRemove()" tdStyle="vertical-align:middle" caption="删除成绩" iconSize="25px" width1="" width2="60px"/]
                    [#else]<td width="80px" style="vertical-align:middle;"><a>未开放录入</a></td>[/#if]
                    [/#if]
                    [#if (makeupGradeTypeState.confirmed)?default(false) || (makeupGradeTypeState.published)?default(false)]
                    [@printHTML url="#" onclick="makeupPrint()" tdStyle="vertical-align:middle"/]
                    [/#if]
                </tr>
                [#if !(makeupGradeTypeState.confirmed)?default(false) && !(makeupGradeTypeState.published)?default(false)][/#if]
            </table>
            </td>
            <td width="20%"></td>
            </tr>
        </table>
        </div>
    </form>
</div>
<script>
	var form3 = document.actionForm3;
    function makeupInput() {
       	bg.form.submit("actionForm3","${b.url('teacher-makeup!input')}");
    }
    
    function makeupRemove() {
        if (confirm("要删除该课程该类型成绩吗？")) {
           	bg.form.submit("actionForm3","${b.url('teacher-makeup!removeGrade')}");
        }
    }
   
    function makeupPrint() {
        bg.form.submit("actionForm3","${b.url('teacher-makeup!report')}");
    }
</script>