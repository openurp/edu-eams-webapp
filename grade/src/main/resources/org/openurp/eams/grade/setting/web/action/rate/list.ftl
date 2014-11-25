[#ftl]
[@b.head/]
[@b.grid items=gradeRateConfigs var="gradeRateConfig"]
	[@b.gridbar]
		bar.addItem("添加设置", action.method("addConfig"));
		bar.addItem("取消设置", action.remove());
		bar.addItem("详细配置", action.single("setting"));
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="scoreMarkStyle.code" title="代码"/]
		[@b.col property="scoreMarkStyle.name" title="名称"]
			${(gradeRateConfig.scoreMarkStyle.name)?if_exists}
			[#--[@b.a href="!info?gradeRateConfigId=${gradeRateConfig.id}" title="查看详细信息"]--]
		[/@]
		[@b.col property="passScore" title="及格线"/]
	[/@]
[/@]
[@b.foot/]
