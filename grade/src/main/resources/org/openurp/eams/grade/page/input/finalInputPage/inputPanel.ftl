[#ftl]
[#if gradeInputSwitch.finalTypes?size > 0 && showFinal]
[#--补考成绩录入--]
<div class="gradeInputTab">
    <div class="gradeInputEntrance">
        <table width="100%" height="25px">
            <tr>
   		     	<td width="35%">
            	</td>
            	<td width="30%" style="text-align:center;">
        			<h2>补考成绩录入</h2>
            	</td>
            	<td width="10%"></td>
				<td width="25%" style="text-align:right">
                </td>
            </tr>
        </table>

        <table align="center" width="100%" cellpadding="0" cellpadding="0">
    		<tr>
    			<td>
    				<table align="center" cellpadding="0" cellpadding="0" class="bottom-icon-buttons">
    				 	<tr>
	                    	[#-- 录入 --]
		    				[#if !showFinal]
		    				<td class="button-icon">
		    					<image src="${base}/static/themes/default/images/icon-info.png" />
		    				</td>
		    				<td class="button-text">
		    					<b>没有补考成绩需要录入</b>
		    				</td>
		    				[#else]
		                    	[#if showFinalButton]
						 			<td class="button-icon">[@inputIcon "icon-enter.png" /]</td>
			    					<td class="button-text">
			    						<a href="${b.url("!inputFinal?lesson.id=" + lesson.id)}">录入</a>
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