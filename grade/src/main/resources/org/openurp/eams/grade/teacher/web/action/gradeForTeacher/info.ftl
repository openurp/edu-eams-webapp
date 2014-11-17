[#ftl]
[@b.head/]
[#assign gradeStatus={'0':'新添加','1':'已确认','2':'已发布'}/]
[#include "/template/macros.ftl"/]
[@b.toolbar title=student.name+"成绩信息"]
   	bar.addBack("${b.text("action.back")}");
[/@]
[#if courseGrades?size > 0]
	[#list courseGrades as grade]
		<table class="infoTable" width="100%">
		  	<tr>
		     	<td class="title">${b.text('attr.taskNo')}</td>
		     	<td class="content">${grade.lessonNo?if_exists}</td>
		     	<td class="title">${b.text('attr.courseNo')}</td>
		     	<td class="content">${grade.course.code}</td>
		     	<td class="title">${b.text('attr.courseName')}</td>
		     	<td class="content">[@i18nName grade.course?if_exists/]</td>
		  	</tr>
		  	<tr>
		     	<td class="title">学年学期</td>
		     	<td class="content">${grade.semester.schoolYear}(${grade.semester.name})</td>
		     	<td class="title">${b.text('attr.credit')}</td>
		     	<td class="content">${(grade.course.credits)?if_exists}</td>
		     	<td class="title">绩点</td>
		     	<td class="content">${(grade.gp?string("#.##"))?if_exists}</td>
		  	</tr>
		  	<tr>
		     	<td class="title">是否通过</td>
		     	<td class="content">[#if grade.passed]是[#else]<font color="red">否[/#if]</td>
		     	<td class="title">状态</td>
		     	<td class="content">${gradeStatus[grade.status?default(0)?string]}</td>
		     	<td class="title">${b.text('entity.markStyle')}</td>
		     	<td class="content">[@i18nName grade.markStyle?if_exists /]</td>
		  	</tr>
		  	<tr>
		     	<td class="title">得分/总评</td>
		     	<td class="content"><span [#if !grade.passed]style="color:red"[/#if]>${(grade.getScore())!}</span></td>
		     	<td class="title">${b.text('entity.courseType')}</td>
		     	<td class="content">[@i18nName grade.courseType?if_exists/]</td>
		     	<td class="title">修读类别</td>
		     	<td class="content">[@i18nName grade.courseTakeType?if_exists/]</td>
		  	</tr>
		  	<tr>
			 	<td class="title">创建时间</td>
		     	<td class="content">${(grade.createdAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
		     	<td class="title">修改时间</td>
		     	<td class="content">${(grade.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
		     	<td class="title">学历层次</td>
		     	<td class="content">${(grade.education.name)?if_exists}</td>
		  	</tr>
		</table>
		[@b.grid items=grade.examGrades var="examGrade"]
			[@b.row]
				[@b.col title="成绩种类"][@i18nName examGrade.gradeType/][/@]
				[@b.col title="记录方式"][@i18nName examGrade.markStyle/][/@]
				[@b.col title="考试情况"][@i18nName examGrade.examStatus/][/@]
				[@b.col title="得分"]<span [#if !examGrade.passed] style="color:red" [/#if]>${(examGrade.getScore())!}<span>[/@]
				[@b.col title="百分比"][#if gradeState?exists]${(gradeState.getPercent(examGrade.gradeType)?string.percent)?if_exists}[/#if][/@]
				[@b.col title="是否通过"][#if examGrade.passed]是[#else]<font color="red">否[/#if][/@]
				[@b.col title="状态"]${gradeStatus[examGrade.status?string]}[/@]
				[@b.col title="更新时间"]${(examGrade.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}[/@]
			[/@]
		[/@]
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
				[#list courseGradeAlterMap.get(grade)?sort_by("updatedAt")?reverse as alterInfo]
			    <tr [#if (alterInfo_index+1)%2==0]class="griddata-odd" [#else]class="griddata-even"[/#if]>
			      <td>最终得分</td>
			      <td>${alterInfo.scoreBefore!}</td>
			      <td>${alterInfo.scoreAfter!}</td>
			      <td>${alterInfo.modifyBy?if_exists.name}</td>
			      <td>${(alterInfo.updatedAt?string("yyyy-MM-dd HH:mm:ss"))?if_exists}</td>
			      <td>${alterInfo.remark?if_exists}</td>
			    </tr>
			    [/#list]
			    [#list examGradeAlterMap.get(grade)?keys as examGrade]
					[#list examGradeAlterMap.get(grade).get(examGrade)?sort_by("updatedAt")?reverse as alterInfo]
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
		<br/>
	[/#list]
[#else]
	暂时没有成绩
[/#if]
[@b.foot/]
