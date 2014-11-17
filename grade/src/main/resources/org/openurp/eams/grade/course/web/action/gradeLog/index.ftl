[#ftl]
[@b.head/]
[@b.toolbar title='成绩日志']
[/@]
[#assign types={"提交":"提交","发布":"发布","取消提交":"取消提交","取消发布":"取消发布","删除":"删除","修改":"修改"} /]
<table class="indexpanel">
	<tr valign="top">
		<td class="index_view" width="20%">
			[@b.form name="searchForm" action="!search" title="ui.searchForm" target="list" theme="search"]
			    [@b.textfield name="gradeLog.student.code" label="学号" maxlength="32"/]
				[@b.textfield name="gradeLog.student.name" label="姓名" maxlength="32"/]
				[@b.textfield name="gradeLog.courseCode" label="课程代码" maxlength="32"/]
				[@b.textfield name="gradeLog.courseName" label="课程名称" maxlength="32"/]
				[@b.textfield name="gradeLog.lessonNo" label="课程序号" maxlength="32"/]
				[@b.select name="gradeLog.type" label="操作类型" items=types empty="..."/]
				[@b.textfield name="gradeLog.operator" label="操作人" maxlength="32" /]
				[@b.startend label="起始日期,截止日期" name="logBegDate,logEndDate" /]
			[/@]
		</td>
		<td valign="top">
			[@b.div id="list" /]
		</td>
	</tr>
</table>

<script>
	jQuery(function() {
		bg.form.submit(document.searchForm);
	});
</script>
[@b.foot/]