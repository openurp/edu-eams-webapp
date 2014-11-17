[#ftl]
[@b.head/]
[@b.toolbar title="成绩记录方式"]
	bar.addBack();
[/@]
<img src="${b.theme.iconurl("actions/keyline.png")}" height="2" width="100%" alt="keyline"/>
[@b.grid items=markStyles var="markStyle" sortable="false" id="test"]
	[@b.row]
		[@b.col property="code" title="代码" width="45%"/]
		[@b.col property="name" title="名称" width="45%"/]
		[@b.col title="操作" width="10%"]<a href="#" onclick="addChoice('${markStyle.id}',this)">添加</a>[/@]
	[/@]
[/@]
<div id="markStylePaseScore" style="display:none;text-align:center;">
	[@b.form name="a" theme="xml" action="!save" target="contentDiv" onsubmit="jQuery.colorbox.close();return true;"]
		[@b.textfield name="gradeRateConfig.passScore" value="" label="及格线"/]<br/>
		[@b.submit style="margin:auto;" value="提交"]
			<input type="hidden" id="markStyleId" name="gradeRateConfig.scoreMarkStyle.id" value=""/>
			<input type="hidden" name="gradeRateConfig.project.id" value="${project.id}"/>
		[/@]
	[/@]
</div>
	[#if !markStyles?exists || markStyles?size == 0]<p>当前没有可以添加的成绩记录方式，或已经全部设置了。</p>[/#if]
	<script>		
		function addChoice(id,ele) {
			jQuery('#markStyleId').val(id);
			jQuery(ele).colorbox({
				speed: 0,
				inline: true, 
				href: "#markStylePaseScore",
				onCleanup: function(){
					jQuery("#markStylePaseScore").hide()
				}
			});
			jQuery("#markStylePaseScore").show();
		}
	</script>
[@b.foot/]

