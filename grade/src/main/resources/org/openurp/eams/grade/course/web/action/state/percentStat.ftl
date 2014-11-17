[#ftl/]
[@b.head/]
[@b.toolbar title="百分比状态统计结果"/]
[@b.grid items=results var="key"]
	[@b.row]
		[@b.col title="录入百分比设定情况"]
			[#assign gradeTypeArray = key?string?split("_")/]
			  	[#if key == "0_0_0_0_0"]
			             <span style="color:red">未设定百分比</span>
	            [#else]
		            [#if gradeTypeArray[0] != "0"]平时成绩 ${gradeTypeArray[0]}％ [/#if]
		            [#if gradeTypeArray[0] != "0" && gradeTypeArray[1] != "0"]，[/#if]
		            [#if gradeTypeArray[1] != "0"]期中成绩 ${gradeTypeArray[1]}％ [/#if]
		            [#if gradeTypeArray[1] != "0" && gradeTypeArray[2] != "0"]，[/#if]
		            [#if gradeTypeArray[2] != "0"]期末成绩 ${gradeTypeArray[2]}％ [/#if]
		            [#if gradeTypeArray[2] != "0" && gradeTypeArray[3] != "0"]，[/#if]
		            [#if gradeTypeArray[3] != "0"]补考成绩 ${gradeTypeArray[3]}％ [/#if]
		            [#if gradeTypeArray[3] != "0" && gradeTypeArray[4] != "0"]，[/#if]
		            [#if gradeTypeArray[4] != "0"]缓考成绩 ${gradeTypeArray[4]}％[/#if]
	            [/#if]
		[/@]
		[@b.col title="对应记录数"]${results[key?string]}[/@]
	[/@]
[/@]
[@b.foot/]