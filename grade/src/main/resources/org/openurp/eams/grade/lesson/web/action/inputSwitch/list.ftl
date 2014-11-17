[#ftl]
[@b.head/]
[@b.grid items=gradeInputSwitchs var="gradeInputSwitch"]
	[@b.gridbar]
		bar.addItem("${b.text('action.new')}", action.add());
		bar.addItem("${b.text('action.delete')}", action.remove());
		bar.addItem("${b.text('action.edit')}", action.edit());
		bar.addItem("${b.text('action.info')}", action.info());
	[/@]
	[@b.row]
		[@b.boxcol width="5%"/]
		[@b.col width="10%" title="attr.year2year" property="semester.schoolYear"]${(gradeInputSwitch.semester.schoolYear)?if_exists}[/@]
		[@b.col width="5%" title="attr.term" property="semester.name"]${(gradeInputSwitch.semester.name)?if_exists?replace('0','第')}[/@]
		[@b.col width="20%" title="开始时间" property="startAt"]${gradeInputSwitch.startAt?string("yyyy-MM-dd HH:mm")!}[/@]
		[@b.col width="20%" title="结束时间" property="endAt"]${gradeInputSwitch.endAt?string("yyyy-MM-dd HH:mm")!}[/@]
		[@b.col width="30%" title="录入成绩类型"][#list gradeInputSwitch.types?sort_by("code") as gradeType]${gradeType.name}[#if gradeType_has_next] [/#if][/#list][/@]
		[@b.col width="10%" title="开关状态" property="opened"][#if gradeInputSwitch.opened][#if gradeInputSwitch.checkOpen()]开放[#else]不在录入时间[/#if][#else]关闭[/#if][/@]
	[/@]
[/@]
[@b.foot/]
