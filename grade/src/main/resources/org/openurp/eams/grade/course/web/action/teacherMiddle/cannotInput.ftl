[#ftl]
[@b.head/]
[@b.toolbar title="成绩开关"]
	bar.addItem("返回","goBack()");
[/@]
[@b.messages slash="4"/]
	<table class="infoTable" align="center">
        <tr>
          	<td class="title" width="30%">教学项目:</td>
		  	<td>${gradeInputSwitch.project.name}</td>
        </tr>
        <tr>
          	<td class="title">学年学期:</td>
		  	<td>${gradeInputSwitch.semester.schoolYear}学年 ${(gradeInputSwitch.semester.name)?if_exists?replace('0','第')}学年</td>
        </tr>
	 	<tr>
	 		<td class="title">是否开放:</td>
	 		<td>${(gradeInputSwitch.opened)?string("开放","关闭")}</td>
	 	</tr>
	 	<tr>
	 		<td class="title">开始时间:</td>
	 		<td> ${(gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm"))?default("")}</td>
	 	</tr>
	 	<tr>
	 		<td class="title">结束时间:</td>
	 		<td>${(gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm"))?default("")}</td>
	 	</tr>
	</table>
	[@b.form name="cannotInputForm" action="/teach/grade/course/teacher!inputTask?lessonId=${lesson.id}" target="contentDiv"/]
<script language="JavaScript">
	function goBack(){
		bg.form.submit("cannotInputForm");
	}
</script>
[@b.foot/]