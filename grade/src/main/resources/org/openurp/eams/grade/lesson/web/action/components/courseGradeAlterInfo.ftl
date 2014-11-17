[#ftl]
[@b.head/]
<table class="gridtable">
	<thead class="gridhead">
	<tr align="center"> 
   		<th>成绩种类</th>
   		<th>修改前</th>
   		<th>修改后</th>
   		<th>修改人</th>
   		<th>修改时间</th>
   		<th>备注</th>
	</tr>
	</thead>
	<tbody>
		[#list courseGradeAlterInfos?sort_by("updatedAt")?reverse as alterInfo]
	    <tr [#if (alterInfo_index+1)%2==0]class="griddata-odd" [#else]class="griddata-even"[/#if]>
	      <td>最终得分</td>
	      <td>${alterInfo.scoreBefore!}</td>
	      <td>${alterInfo.scoreAfter!}</td>
	      <td>${alterInfo.modifyBy?if_exists.name}</td>
	      <td>${(alterInfo.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
	      <td>${alterInfo.remark?if_exists}</td>
	    </tr>
	    [/#list]
	    [#list examGradeAlterInfos?keys as examGrade]
			[#list examGradeAlterInfos.get(examGrade)?sort_by("updatedAt")?reverse as alterInfo]
		 	<tr [#if (courseGrade.alterInfos?size+(examGrade_index+1)*alterInfo_index+1)%2==0]class="griddata-odd" [#else]class="griddata-even"[/#if]>
		   		<td>${examGrade.gradeType.name!}</td>
		      	<td>${alterInfo.scoreBefore!}</td>
		      	<td>${alterInfo.scoreAfter!}</td>
		      	<td>${alterInfo.modifyBy?if_exists.name}</td>
		      	<td>${(alterInfo.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
		      	<td>${alterInfo.remark?if_exists}</td>
		  	</tr>
	      	[/#list]
	    [/#list]
	</tbody>
</table>
[@b.foot/]