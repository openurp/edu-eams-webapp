<#include "/template/head.ftl"/>
<body>
	<table id="bar" width="100%"></table>
	<table width="100%" class="frameTable">
		<tr valign="top">
		<form method="post" action="" name="actionForm">
			<td width="20%" class="frameTable_view"><#include "searchForm.ftl"/></td>
		</form>
			<td><iframe src="#" id="iframeId" name="pageIFrame" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" height="100%" width="100%"></iframe></td>
		</tr>
	</table>
	<script>
		var bar = new ToolBar("bar", "成绩未通过的学分统计", null, true, true);
		bar.setMessage('<@getMessage/>');
		bar.addBlankItem();
		
		var form = document.actionForm;
		function search() {
			form.action = "gradeFailCreditStats.action?method=search";
			form.target = "pageIFrame";
			form.submit();
		}
		function init() {
			form.action = "gradeFailCreditStats.action?method=initData";
			form.target = "pageIFrame";
			form.submit();
		}
		init();
	</script>
</body>
<#include "/template/foot.ftl"/>
