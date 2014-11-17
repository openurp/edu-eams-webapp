[#ftl]
[#if gradeInputSwitch.gaDelayTypes?size > 0 && showGaDelay]
[#--缓考成绩录入--]
[#assign gaGradeState = courseGradeState.getState(GRADE_TYPE_GA)!/]
<div class="gradeInputTab">
    <div class="gradeInputEntrance">
        <table width="100%" height="25px">
            <tr>
            	<td width="20%">
            	</td>
            	<td width="60%" style="text-align:center;">
        			<h2>缓考成绩录入</h2>
            	</td>
				<td width="20%" style="text-align:right">
                </td>
            </tr>
        </table>

        <table align="center" width="100%" cellpadding="0" cellpadding="0">
    		<tr>
    			<td>
    				<table align="center" cellpadding="0" cellpadding="0" class="bottom-icon-buttons">
    				 	<tr>
	                    	[#-- 录入 --]
		    				[#if !showGaDelay]
		    				<td class="button-icon">
		    					<image src="${base}/static/themes/default/images/icon-info.png" />
		    				</td>
		    				<td class="button-text">
		    					<b>没有缓考成绩需要录入</b>
		    				</td>
		    				[#else]
		                    	[#if showGaDeplayButton]
						 			<td class="button-icon">[@inputIcon "icon-enter.png" /]</td>
			    					<td class="button-text">
			    						<a href="${b.url("!inputGADelay?lesson.id=" + lesson.id)}">录入</a>
			    					</td>
			    				[#else]
			    					<td class="button-icon"><image src="${base}/static/themes/default/images/icon-ok.png" width="25px"/></td>
			    					<td class="button-text">
				    					已提交
			    					</td>
		    					[/#if]
		    				[/#if]
    				 	</tr>
    				 </table>
    			</td>
			</tr>
        </table>
    </div>
</div>
[/#if]