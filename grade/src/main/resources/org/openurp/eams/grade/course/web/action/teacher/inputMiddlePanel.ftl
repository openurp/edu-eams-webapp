[#ftl]
[#--期中成绩录入--]
<div class="dynamic-tab-pane-control tab-pane" id="tabPane1">
    <script type="text/javascript">tp1 = new WebFXTabPane(document.getElementById("tabPane1"), false);</script>
    <div style="display: block;height:100px;padding:5px;font-size:14px;background-color:#E1ECFF;;border-color:LightSkyBlue" class="tab-page" id="tabPage1_1">
        <h2 class="tab" style="background-color:#E1ECFF;font-size:14px;border-color:LightSkyBlue;width:90px;"><b>期中成绩录入</b></h2>
        <script type="text/javascript">tp1.addTabPage(document.getElementById("tabPage1_1"));</script>
        <table width="100%" height="25px">
            <tr>[#assign middeGradeTypeState = gradeState.getState(MIDDLE)!/]
            	[@large_stateinfo status=(middeGradeTypeState.status!0) /]
                <td align="right">[#if middeGradeTypeState?? && middeGradeTypeState.inputedAt??]上次录入:${middeGradeTypeState.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
            </tr>
        </table>
         <form method="post" action="" name="actionForm1" id="actionForm1" target="_self">
        <table align="center" style="text-align:center" cellpadding="0" cellspacing="0">
	        <input type="hidden" name="lessonId" value="${lesson.id}"/>
	        <input type="hidden" name="gradeTypeIds" value="${MIDDLE.id}"/>
            <tr>
                [@gradeInfoHTML url="#" onclick="gradeInfo(document.actionForm1)" tdStyle1="" tdStyle2="text-align:left" caption="查看" iconSize="25px" width1="" width2="35px"/]
                [#if !(middeGradeTypeState.confirmed)?default(false)]
                [@inputHTML url="#" tdStyle1="" tdStyle2="text-align:left" onclick="middleInput()" caption="录入" iconSize="25px" width1="" width2="35px"/]
                [@removeGradeHTML url="#" onclick="middleRemove()" tdStyle="text-align:left" caption="删除" iconSize="25px" width1="" width2="35px"/]
                [/#if]
                [#if (middeGradeTypeState.confirmed)?default(false)]
                [@printHTML url="#" onclick="middlePrint()" tdStyle=""/]
                [/#if]
            </tr>
        </table>
        </form>
    </div>
</div>
<script>
	function middleInput() {
		var form1 = document.actionForm1;
		[#if !gradeState.confirmed]
    	bg.form.addInput(form1,"precision",document.getElementById("precision").value);
    	bg.form.addInput(form1,"markStyleId",document.getElementById("markStyleId").value);
    	[/#if]
        bg.form.submit("actionForm1","${b.url('teacher-middle!input')}");
	}
       
	function middleRemove() {
	    if (confirm("要删除该课程所有期中成绩吗？")) {
	        bg.form.submit("actionForm1","${b.url('teacher-middle!removeGrade')}");
	    }
	}
    function middlePrint() {
        bg.form.submit("actionForm1","${b.url('teacher-middle!report')}");
    }
</script>