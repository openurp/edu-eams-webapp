[#ftl]
[#if gradeInputSwitch.gaTypes?size > 0]
[#--总评成绩录入--]
[#assign gaGradeState = courseGradeState.getState(GRADE_TYPE_GA)!/]
<div class="gradeInputTab">
    <div class="gradeInputEntrance">
        <table width="100%" height="25px">
            <tr>
            	<td width="35%"></td>
            	<td width="30%" style="text-align:center;">
            		<h2>总评成绩录入</h2>
        			[#if !gaGradeState.beyondSubmit]
            		本次开放录入：[#list gradeInputSwitch.gaTypes?sort_by("code") as g]${g.name}[#if g_has_next], [/#if][/#list]
            		[/#if]
            	</td>
            	<td width="10%">
            	<h2>[@gradeStatusIcon gaGradeState /]</h2>
            	</td>
				<td width="25%" style="text-align:right">
            	[#if (gaGradeState.inputedAt)??]上次录入:${gaGradeState.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]
                </td>
            </tr>
            <tr>
            	<td colspan="3">
            		<h2></h2>
            	</td>
            </tr>
        </table>

	<form method="post" action="" id="gaInputForm" name="gaInputForm">
        <table align="center" width="100%" cellpadding="0" cellpadding="0">
        	[#-- TODO 好好整理这些参数 --]
		    <input type="hidden" name="lessonId" value="${lesson.id}"/>
		    <input type="hidden" name="lessonIds" value="${lesson.id}"/>
		    <input type="hidden" name="kind" value="" />
		    <input type="hidden" name="gradeTypeIds" value="[#list courseGradeSetting.gaElementTypes as t]${t.id},[/#list]${GRADE_TYPE_GA!}"/>
    	[#list courseGradeSetting.gaElementTypes?sort_by("code") as gradeType]
         	<tr>
         		<td width="30%"></td>
                [#assign examGradeState = courseGradeState.getState(gradeType)]
                <td style="vertical-align:middle;text-align:right" width="10%">
                  ${gradeType.name}:
                </td>
            	[#if gaGradeState.beyondSubmit || examGradeState.beyondSubmit]
                <td class="underscore" width="10%">
                	${(examGradeState.percent)?default(0) * 100}
                	<input type="hidden" name="examGradeState${gradeType.id}.percent" value="${(examGradeState.percent)?default(0) * 100}"/>
                </td>
                [#else]
                <td width="10%">
                	<input type="text" name="examGradeState${gradeType.id}.percent" value="${(examGradeState.percent)?default(0) * 100}" style="width:150px;text-align:right" maxlength="3"/>
                </td>
                [/#if]
                <td width="1%">％</td>
                <td width="10%">
                	[#if examGradeState.beyondSubmit || gaGradeState.beyondSubmit]
                		${(examGradeState.scoreMarkStyle.name)!}
                		<input type="hidden" name="examGradeState${gradeType.id}.scoreMarkStyle.id" value="${(examGradeState.scoreMarkStyle.id)!}" />
                	[#else]
                		[@b.select name="examGradeState${gradeType.id}.scoreMarkStyle.id" items=markStyles value=(examGradeState.scoreMarkStyle.id)?if_exists style="width:150px"/]
                	[/#if]
                </td>
                <td width="30%">
                	[@gradeStatusIcon examGradeState /]
                </td>
            </tr>
    	[/#list]
    		<tr>
    			<td colspan="6">&nbsp;</td>
    		</tr>
    		<tr>
    			<td colspan="6">
    				[#-- 不把图片和文字分开，不能够vertical-align --]
    				<table align="center" cellpadding="0" cellpadding="0" class="bottom-icon-buttons">
    				 	<tr>
    				 		[#-- 空白登分表 --]
    				 		<td class="button-icon">[@inputIcon "icon-info.png" /]</td>
    				 		<td class="button-text">
								<a href="${b.url('!blank?lesson.id='+lesson.id)}">空白登分表</a>
    				 		</td>
    				 		[#-- 查看成绩 --]
    				 		<td class="button-icon">[@inputIcon "icon-info.png" /]</td>
    				 		<td class="button-text">
 								<a href="${b.url('!info?lesson.id='+lesson.id)}">查看</a>
    				 		</td>
    				 		[#-- 打印分段统计表 --]
	                        [#if gaGradeState.beyondSubmit]
				 			<td class="button-icon">[@inputIcon "icon-printer.png" /]</td>
	    					<td class="button-text">
								<a href="#" id="ga-stat-button">分段统计表</a>
							</td>
	                        [/#if]
		                    [#if !gaGradeState.beyondSubmit]
		                    	[#-- 录入 --]
    				 			<td class="button-icon">[@inputIcon "icon-enter.png" /]</td>
		    					<td class="button-text">
		    						<a href="#" id="ga-input-button">录入</a>
		    					</td>
		                    [#else]
		                   		[#-- 打印 --]
    				 			<td class="button-icon">[@inputIcon "icon-printer.png" /]</td>
		    					<td class="button-text">
		    						<a href="${b.url('!report?lesson.id='+lesson.id)}">打印</a>
		    					</td>
		                    [/#if]
    				 	</tr>
    				 </table>
    			</td>
			</tr>
        </table>
	</form>
    </div>
</div>
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<script>
jQuery(function() {
	// 成绩录入按钮
	jQuery("#ga-input-button").click(function(event) {
		event.preventDefault();
		
		var totalPercent=0;
        var onePercent="";
        [#list courseGradeSetting.gaElementTypes as gradeType]
        onePercent = document.gaInputForm["examGradeState${gradeType.id}.percent"].value;
        if("" != onePercent ){
          if(!/^\d+$/.test(onePercent)){
            alert("${gradeType.name}不是正整数的数值格式");
            return;
          } else {
            totalPercent += parseFloat(onePercent);
          }
        }
        [/#list]
        if(totalPercent != 100) {
            alert("所有设置的百分比数值之和"+totalPercent+"%,应为100％");
            return;
        }
        [#if !courseGradeState.beyondSubmit]
        bg.form.addInput(document.gaInputForm, "markStyleId", document.getElementById("markStyleId").value, "hidden");
        bg.form.addInput(document.gaInputForm, "precision",   document.getElementById("precision").value, "hidden");
        [/#if]
        document.gaInputForm.target = "_self";
        bg.form.submit("gaInputForm","${b.url('!inputGA')}");
	});
	
	// 成绩分段统计按钮
	jQuery("#ga-stat-button").click(function(event) {
		event.preventDefault();
		document.gaInputForm.action = "${b.url('!stat')}";
		document.gaInputForm["kind"].value = "lesson";
        for(var i=0;i<seg.length;i++){
          var segAttr="segStat.scoreSegments["+i+"]";
          bg.form.addInput(document.gaInputForm,segAttr+".min",seg[i].min);
          bg.form.addInput(document.gaInputForm,segAttr+".max",seg[i].max);
        }
        bg.form.addInput(document.gaInputForm,"scoreSegmentsLength",seg.length);
        document.gaInputForm.target="_blank";
        document.gaInputForm.submit();
	});
});
</script>
[/#if]