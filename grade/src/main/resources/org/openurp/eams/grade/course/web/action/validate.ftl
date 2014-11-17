[#ftl]
[@b.head/]
[@b.toolbar title="${b.text('grade.teachClassInput')}"]
	 bar.addClose("${b.text("action.close")}");
[/@]
<link href="${base}/static/css/tab.css" rel="stylesheet" type="text/css">
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/common/TabPane.js"></script>
<style>
.gradePanel{
    background-color :#c7dbff;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
}
</style>
<br/>
    <table class="gradePanel" align="center" style="text-align:center;width:95%">
        <tr>
            <td align="center">
                <table style="padding:2%;width:100%">
                    <tr>
                        <td width="100px">课程序号:</td>
                        <td width="150px">${lesson.no}</td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>课程名称:</td>
                        <td>${lesson.course.name}</td>
                        <td>
	                        [#if (gradeInputSwitch.startAt)??]允许录入:
	                        	[#list gradeInputSwitch.types?sort_by("code") as gradeType]
	                        	${gradeType.name}
	                        	[/#list]
	                        [#else]
	                        	未开放录入
	                        [/#if]
                       	</td>
                    </tr>
                    <tr>
                        <td>开始时间:</td>
                        <td>
                        	[#if (gradeInputSwitch.startAt)??]
                        		${gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm")}
                        	[/#if]
                        </td>
                        <td></td>
                    </tr>
                        <td>
                        	截止时间:
                        </td>
                        <td>
                        	[#if (gradeInputSwitch.startAt)??]${gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm")}[/#if]
                        </td>
                        <td></td>
                    </tr>
                     <tr>
                        <td colspan="3">
                        	<div class="dynamic-tab-pane-control tab-pane" id="tabPane2">
							    <script type="text/javascript">tp2 = new WebFXTabPane(document.getElementById("tabPane2"), false);</script>
							    <div style="display: block;height:145px;padding:5px;font-size:14px;background-color:#E1ECFF;border-color:LightSkyBlue" class="tab-page" id="tabPage2_1">
                        			<h2 class="tab" style="background-color:#E1ECFF;font-size:14px;border-color:LightSkyBlue;width:90px;"><b>成绩录入</b></h2>
							        <script type="text/javascript">tp2.addTabPage(document.getElementById("tabPage2_1"));</script>
							        [@b.form name="validateForm" target="_self"]						    
							        <table align="center" width="100%">
							            <tr valign="top">
							                <td width="20%" align="center">
							                    <table class="gridtable" style="table-layout:auto;margin-top:30px;width:60%">
													<thead class="gridhead">
														<tr>
															<th>
																请输入验证码([#if challenge?? && !challenge.expired]${challenge.effectiveAt?string("HH:mm")}发送,[/#if]${timeToLiveMinutes}分钟过期)
															</th>
														</tr>
													</thead>
													<tbody>
														<tr>
															<td align="center">
																<label for="message">验证码：</label><input id="message" name="userresponse" value=""/>
																<span id="validateMsg">[#if challenge??&&Parameters['userresponse']??][#if challenge.expired]验证码已过期[#else]验证码填写有误[/#if][/#if]</span>
															</td>
														</tr>
														<tr>
															<td align="center">
																<input type="button" value="提交" onClick="redirectURL('${servletPath}')"/>
																<input type="button" value="重新发送" onClick="sendMessage()"/>
																[#list Parameters?keys as k]
																	[#if k != 'userresponse' && k!="params" && Parameters[k]!='']
																		<input name="${k}" value="${Parameters[k]}" type="hidden" class="queryString"/>
																	[/#if]
																[/#list]
															</td>
														</tr>
													</tbody>
							                    </table>
							                </td>
							            </tr>
							        </table>
							        [/@]
							    </div>
							</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>

<script language="JavaScript">
	function genQueryString(){
		var queryStr = "";
		jQuery(".queryString").each(function(i){
			if(i != 0){	
				queryStr+="&";
			}
			queryStr += this.name+"="+this.value;
		})
		return queryStr;
	}
	
	function redirectURL(url){
		var form = document.validateForm;
		if(!jQuery("#message").val()){
			alert("请输入验证码");
			return;
		}
		form.action="${base}"+url;
		bg.form.submit(form);
	}
	
	function sendMessage(){
		jQuery.ajax({
				type: "post",
				url : "${b.url('/mailValidate!send')}?resource=${servletPath}&params="+encodeURIComponent(genQueryString()),
				error : function() { alert('响应失败!'); },
				dataType : "text",
				async : false,
				success : function(result) {
					jQuery("#validateMsg").text(result);
				}
			});
	}
	
	[#if (challenge?? && challenge.expired) || !(challenge??)]
	jQuery(document).ready(function(){
		sendMessage();
	})
	[/#if]
</script>
[@b.foot/]