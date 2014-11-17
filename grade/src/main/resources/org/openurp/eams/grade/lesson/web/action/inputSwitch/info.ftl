[#ftl]
[@b.head/]
[@b.toolbar title="详情查看"]
	bar.addBlankItem();
	bar.addBack();
[/@]
	<table class="infoTable" width="100%" align="center">
        <tr>
          	<td class="title" width="20%">学年学期:</td>
		  	<td>${(gradeInputSwitch.semester.schoolYear)?if_exists}学年${(gradeInputSwitch.semester.name)?if_exists?replace('0','第')}学期</td>
	 		<td class="title">开关状态:</td>
	 		<td>${(gradeInputSwitch.opened)?if_exists?string("开放","关闭")}</td>
        </tr>
	 	<tr>
	 		<td class="title">开放成绩类型:</td>
	 		<td colspan="3">[#list gradeInputSwitch.types?sort_by("code") as gradeType]${(gradeType.name)?if_exists}[#if gradeType_has_next],[/#if][/#list]</td>
	 	</tr>
	 	<tr>
	 		<td class="title">开始时间:</td>
	 		<td> ${(gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm"))?default("")}</td>
	 		<td class="title">结束时间:</td>
	 		<td>${(gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm"))?default("")}</td>
	 	</tr>
	</table>
[@b.foot/]
