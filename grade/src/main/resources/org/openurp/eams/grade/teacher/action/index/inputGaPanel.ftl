[#ftl]
[#--总评成绩录入--]
[#assign gaGradeTypeState = gradeState.getState(EndGa)!/]
<div class="dynamic-tab-pane-control tab-pane" id="tabPane2">
    <div style="display: block;height:200px;padding:5px;font-size:14px;background-color:#E1ECFF;border-color:LightSkyBlue" class="tab-page" id="tabPage2_1">
        <h2 class="tab" style="background-color:#E1ECFF;font-size:14px;border-color:LightSkyBlue;width:90px;"><b>总评成绩录入</b></h2>
        <table width="100%" height="25px">
            <tr>
            	[@large_stateinfo status=(gradeState.getState(EndGa).status)!0/]
                <td align="right">[#if (gradeState.getState(EndGa).inputedAt)??]上次录入:${gradeState.getState(EndGa).inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
            </tr>
        </table>
        <form method="post" action="" id="actionForm2" name="actionForm2">
        <table align="center" width="100%" cellpadding="0" cellpadding="0" >
		    <input type="hidden" name="lessonId" value="${lesson.id}"/>
		    <input type="hidden" name="lessonIds" value="${lesson.id}"/>
		    <input type="hidden" name="gradeTypeIds" value="[#list gaGradeTypes as t]${t.id},[/#list]${EndGa.id}"/>
		    <input type="hidden" name="kind" value="task"/>
		    <input type="hidden" name="isChangeGA" value="1"/>
            <tr valign="top">
                <td width="20%">
                    <table cellpadding="0" cellspacing="0" >
                        <tr>
                            [@gradeInfoHTML url="${b.url('/teach/grade/lesson/teacherReport!blank?lesson.id='+lesson.id)}" onclick="" tdStyle1="" tdStyle2="text-align:left" caption="空白登分表" iconSize="25px" width1="" width2="100px"/]
                        </tr>
                        [#--
                        [@ems.guard res="/teach/grade/course/teach-quality"]
                        <tr>
                            [#if ((gaGradeTypeState.confirmed)!false)]
                            [@gradeInfoHTML url="#" onclick="printExamReport()" tdStyle1="" tdStyle2="text-align:left" caption="教学质量分析表" iconSize="" width1="" width2="100px"/]
                            [/#if]
                        </tr>
                        		[/@]
                        [@ems.guard res="/teach/grade/lesson/report"]
						--]                        		
                        <tr>
                            [#if ((gaGradeTypeState.confirmed)!false)]
                            [@gradeInfoHTML url="#" onclick="printStatReport('lesson')" tdStyle1="" tdStyle2="text-align:left" caption="打印分段统计表" iconSize="" width1="" width2="100px"/]
                            [/#if]
                        </tr>
                        [#--[/@]--]
                    </table>
                </td>
                <td>
                    <table cellpadding="0"  align="center"  >
                    	[#list gaGradeTypes?sort_by("code") as gradeType]
                        [#if !gradeType.ga]
	                     	<tr>
	                            [#assign gradeTypeState = gradeState.getState(gradeType)!]
	                            <td style="vertical-align:middle;text-align:right" width="70px">
	                              ${gradeType.name}:
	                            </td>
	                            [#if ((gradeTypeState.confirmed)!false) || ((gaGradeTypeState.confirmed)!false)]
	                            <td style="border-bottom-width:1px;border-bottom-color:black;border-bottom-style:solid;text-align:center" width="150px">${(gradeTypeState.percent)?default(0) * 100}<input type="hidden" name="examGradeState${gradeType.id}.percent" value="${(gradeTypeState.percent)?default(0) * 100}"/></td>
	                            [#else]
	                            <td><input type="text" name="examGradeState${gradeType.id}.percent" value="${(gradeTypeState.percent)?default(0)}" style="width:150px;text-align:right" maxlength="3"/></td>
	                            [/#if]
	                            <td>％</td>
	                            <td style="color:blue">
	                            	<table cellpadding="0" cellpadding="0"><tr>[@small_stateinfo status=(gradeTypeState.status!0)/]</tr></table>
	                            </td>
	                        </tr>
	                      [/#if]
                    	[/#list]
                    </table>
                    <table align="center" align="center" cellpadding="0" cellspacing="0">
                        <tr height="50px" valign="bottom">
                            [@gradeInfoHTML url="#" onclick="gradeInfo(document.actionForm2)" tdStyle1="vertical-align:middle;" tdStyle2="vertical-align:middle;" caption="查看" iconSize="25px" width1="" width2="40px"/]
                            [#if !((gaGradeTypeState.confirmed)!false)]
                              [#if gradeInputSwitch.open || true]
                            [@inputHTML url="#" tdStyle1="vertical-align:middle" tdStyle2="vertical-align:middle;text-align:left" onclick="gaInput()" caption="录入" iconSize="25px" width1="" width2="40px"/]
                            [@removeGradeHTML url="#" onclick="gaRemove()" tdStyle="vertical-align:middle" caption="删除成绩" iconSize="25px" width1="" width2="60px"/]
                              [#else]<td width="80px" style="vertical-align:middle;"><a>未开放录入</a></td>
                              [/#if]
                            [#else]
                            [/#if]
                            [@printHTML url="#" onclick="gaPrint()" tdStyle="vertical-align:middle"/]
                        </tr>
                    </table>
                </td>
                <td width="20%"></td>
            </tr>
        </table>
		</form>
    </div>
</div>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script>
	var form2 = document.actionForm2;
    function gaInput() {
    	var totalPercent=0;
        var onePercent="";
        [#list gaGradeTypes as gradeType]
          [#if !gradeType.ga]
        onePercent = form2["examGradeState${gradeType.id}.percent"].value;
        if("" != onePercent ){
          if(!/^\d+$/.test(onePercent)){
            alert(autoLineFeed("${gradeType.name}不是正整数的数值格式"));
            return;
          }else{
            totalPercent += parseFloat(onePercent);
          }
        }
          [/#if]
        [/#list]
        if(totalPercent != 100) {
            alert("所有设置的百分比数值之和"+totalPercent+"%,应为100％");
            return;
        }
        [#if !gradeState.confirmed]
        bg.form.addInput(form2, "markStyleId", document.getElementById("markStyleId").value, "hidden");
        [/#if]
        form2.target = "_self";
        bg.form.submit("actionForm2","${b.url('end-ga!input?lesson.id=${lesson.id}')}");
    }
       
    function gaRemove() {
        if (confirm("要删除该课程所有总评成绩及其组成部分吗？")) {
            form2.target = "_self";
            bg.form.submit("actionForm2","${b.url('!removeGrade')}");
        }
    }
    
    function gaPrint() {
        form2.target = "self";
       	bg.form.submit("actionForm2","${b.url('/eams/grade/teacher/report')}");
    }
        
    //打印分段统计
    function printStatReport(kind) {
        form2.action = "${b.url('/teach/grade/lesson/teacherReport!stat')}";
        if (null != kind) {
           form2["kind"].value = kind;
        }
        for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          bg.form.addInput(form2,segAttr+".min",seg[i].min);
          bg.form.addInput(form2,segAttr+".max",seg[i].max);
        }
        bg.form.addInput(form2,"scoreSegmentsLength",seg.length);
        form2.target="_blank";
        form2.submit();
    }
    [#--
    //打印试卷分析
    function printExamReport() {
        form2.action = "${b.url("teach-quality!addTeachQuilty?lesson.id=${lesson.id}")}";
        form2.target = "_blank";
        [#include "../components/segScore.ftl"/]
        [@addSeqToForm formName='form2'/]
        form2.submit();
    }
    --]
    function gradeInfo(form) {
        form.action = "${b.url('!info?id=${lesson.id}')}";
        //form.target = "_self";
        bg.form.submit(form, "${b.url('!info?id=${lesson.id}')}");
    }
</script>