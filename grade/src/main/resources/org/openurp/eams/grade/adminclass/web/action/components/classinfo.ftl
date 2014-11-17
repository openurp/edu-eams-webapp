[#ftl/]
<table class="infoTable" style="width:70%;margin-left:20px;">
	<tr>
		<td class="title" with="20%">班级名称:</td>
		<td class="brightStyle" width="30%">${(adminclass.name)!}</td>
		<td class="title" with="20%">班级代码:</td>
		<td class="brightStyle" width="30%">${(adminclass.code)!}</td>
	</tr>
	<tr>
		<td class="title">院系:</td>
		<td>${(adminclass.department.name)!}</td>
		<td class="title">学生类别:</td>
		<td>${(adminclass.stdType.name)!}</td>
	</tr>
	<tr>
		<td class="title">专业:</td>
		<td>${(adminclass.major.name)!}</td>
		<td class="title">方向:</td>
		<td>${(adminclass.direction.name)!}</td>
	</tr>
	<tr>
		<td class="title">计划人数:</td>
		<td>${(adminclass.planCount)!}</td>
		<td class="title">实际人数:</td>
		<td>${(adminclass.stdCount)!}</td>
	</tr>
</table>
<div style="width:70%;margin-left:20px;background-color:#E1ECFF;text-align:center">
	<b>班级学生列表</b>
</div>
	<div style="width:70%;margin-left:20px;">
		[#assign students = adminclass.students?sort_by("code")/]
		[#list students as student]
			[#if student_index%3==0]
				<table align="center" width="100%" style="border:1px solid #A6C9E2;">
					<tr>
			[/#if]	
		    	<td width="30%" style="background-color:#e8eefa;">
					<table width="100%">
				      <tr>
				        <td rowspan="3" width="80px"><img src="${base}/avatar/user.action?user.name=${(student.code)!}" width="40px" height="55px"/></td>
				      </tr>
				      <tr>
				           <td>[@b.a href="/studentSearch!info?studentId=${(student.id)!}" target="_blank"  title="查看学生详细信息"]${(student.code)!}[/@]</td>
				      </tr>
				      <tr>
				           <td>
					           ${(student.name)!}&nbsp;
					           ${(student.gender.name)!}
				           </td>
				      </tr>
			 		</table>
			 	</td>
			[#if student_index%3==2]
					</tr>
				</table>
			[/#if]
			[#if (students?size)%3==1&&student_index==(students?size-1)]
					</tr>
				</table>
			[/#if]
			[#if (students?size)%3==2&&student_index==(students?size-1)]
						<td width="30%" style="background-color:#e8eefa;">
							<table width="100%">
						      <tr><td>&nbsp;</td></tr>
						      <tr><td>&nbsp;</td></tr>
						      <tr><td>&nbsp;</td></tr>
					 		</table>
					 	</td>
			 		</tr>
				</table>
			[/#if]
		[/#list]
		[#if (students!?size)==0]
			<div align="center" style="color:#666666;background:#E1ECFF;"><b>该班级没有学生!</b></div>
		[/#if]
	</div>