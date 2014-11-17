[#ftl /]
[@b.head/]
[@b.toolbar title='${b.text("学分不过半学生列表")}']
	bar.addClose();
[/@]
[#include "/template/macros.ftl"/]
[@b.grid items=stdTCs var="stdTC"]
	[@b.gridbar]
		bar.addItem("学生信息", "info()");
		bar.addPrint("${b.text("action.print")}");
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="std.code" title="学号" width="8%"]
			[@b.a href="studentSearch!info.action?studentId=${stdTC.std.id}" target="_blank" title="查看学生基本信息"]${stdTC.std.code}[/@]
		[/@]
		[@b.col property="std.name" title="姓名" width="7%" ][@i18nName stdTC.std /][/@]
		[@b.col property="stdType.name" title="学生类别" width="20%"][@i18nName (stdTC.std.stdType)?if_exists/][/@]
		[@b.col property="std.department.name" title="院系" width="15%"][@i18nName (stdTC.std.department)?if_exists/][/@]
		[@b.col property="std.major.name" title="专业" width="15%"][@i18nName (stdTC.std.major)?if_exists/][/@]
		[@b.col property="std.direction.name" title="方向" width="15%"][@i18nName (stdTC.std.direction)?if_exists/][/@]
		[@b.col property="credits" title="已得学分" width="15%"/]
		[@b.col property="totalCredits" title="总学分" width="15%"/]
	[/@]
[/@]					
[@b.foot /]
<SCRIPT LANGUAGE="javascript">
	var form = document.actionForm;
		function info() {
			form.action = "studentDetailByManager.action?method=detail";
			submitId(form, "stdId", false);
		}
</SCRIPT>
