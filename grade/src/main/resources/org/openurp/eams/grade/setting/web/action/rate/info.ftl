[#ftl]
[@b.head/]
[@b.toolbar title="<font color=\"blue\">${(gradeRateConfig.markStyle.name)?if_exists}</font>分数配置详细信息"]
	bar.addBackOrClose("${b.text('action.back')}", "${b.text('action.close')}");
[/@]
	<table width="90%" cellpadding="0" cellspacing="0" align="center">
		<tr>
			<td>
								<table width="100%" class="infoTable">
			  		<tr>
			            <td class="title">代码:</td>
			  		    <td class="content">${gradeRateConfig.scoreMarkStyle.code}</td>
			            <td class="title">中文名称:</td>
			  		    <td class="content">${gradeRateConfig.scoreMarkStyle.name}</td>
			  		</tr>
			  		<tr>
			            <td class="title">及格线:</td>
			  		    <td class="content">${gradeRateConfig.passScore}</td>
			            <td class="title">英文名称:</td>
			  		    <td class="content">${(gradeRateConfig.scoreMarkStyle.engName)?if_exists}</td>
			  		</tr>
				</table>
				[#assign configItems = gradeRateConfig.items/]
				[@b.grid items=configItems var="configItem"]
					[@b.row]
						[@b.col title="分数名称"]${(configItem.grade?html)?if_exists}[/@]
						[@b.col title="最小值(含)"]${configItem.minScore?string("0.##")}[/@]
						[@b.col title="最大值(含)"]${configItem.maxScore?string("0.##")}[/@]
						[@b.col title="默认值"]${configItem.defaultScore?string("0.##")}[/@]
						[@b.col title="绩点"]${(configItem.gpExp)!}[/@]
					[/@]
				[/@]
			</td>
		</tr>
	</table>
[@b.foot/]
