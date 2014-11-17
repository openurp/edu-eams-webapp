[#ftl]
[@b.head/]
<link rel="stylesheet" type="text/css" href="${base}/static/css/ext-like-table.css" />


<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/chosen/ajax-chosen.js"></script>
[#include "/template/macros.ftl"]
[@b.toolbar title='其他录入人批量设置']
	bar.addBack("${b.text('action.back')}");
[/@]
[@b.form name="batchSetTeacherForm"action="!batchSaveExtraInputer"]
<table width="100%" class="extTable">
	<caption class="normal">
		本次修改数量：${courseGradeStates?size}
		&nbsp;&nbsp;
		<input type='button' value='保存' onClick="bg.form.submit('batchSetTeacherForm');"/>
	</caption>
	<thead>
		[#--
		<tr>
			<th colspan="6">批量操作</th>
			<th>
				<select id="allextraInputer" style="width:90%" onchange="topCourseTypeChange();">
				</select>
			</th>
		</tr>
		--]
		<tr>
			<th width="10%">${b.text("attr.taskNo")}</th>
			<th width="15%">课程名称</th>
			<th width="13%">课程代码</th>
			<th width="15%">课程类别</th>
			<th width="12%">开课院系</th>
			<th width="15%">${b.text("entity.teacher")}</th>
			<th width="20%">其他录入人</th>
		</tr>
	</thead>
	<tbody>
	[#list courseGradeStates?sort_by(['lesson','no'])! as courseGradeState]
		<tr align="center">
			<td>${courseGradeState.lesson.no}<input type="hidden" value="${courseGradeState.id}" name="courseGradeState.id"/></td>
			<td>
				${(courseGradeState.lesson.course.name)!}
			</td>
			<td>
				${courseGradeState.lesson.course.code}
			</td>
			<td>
				${courseGradeState.lesson.courseType.name}
			</td>
			<td>
				${courseGradeState.lesson.teachDepart.name}
			</td>
			<td>
				[@getTeacherNames courseGradeState.lesson.teachers/]
			</td>
			<td>
				<select id="extraInputer_${courseGradeState_index}" role="extraInputer" name="courseGradeState.extraInputer.id${courseGradeState.id}" style="width:95%">
					<option value="">...</option>
					[#if courseGradeState.extraInputer??]
						<option value='${courseGradeState.extraInputer.id}' selected>${courseGradeState.extraInputer.fullname}(${courseGradeState.extraInputer.name})</option>
					[/#if]
				</select>
			</td>
		</tr>
	[/#list]
	</tbody>
	<tr>
		<td class="normal" colspan="7">
			本次修改数量：${courseGradeStates?size}
			&nbsp;&nbsp;
			<input type='button' value='保存' onClick="bg.form.submit('batchSetTeacherForm');"/>
		</td>
	</tr>
</table>
[/@]
<script>
	jQuery(document).ready(function(){
		jQuery("select[role='extraInputer']").each(function(){
			jQuery(this).ajaxChosen(
				{
					method: 'post',
					url: "${base}/teach/grade/lesson/input!searchUsers.action?pageNo=1&pageSize=10"
				}
				, function (data) {
					var items = {};
					var dataObj = eval("(" + data + ")");
					jQuery.each(dataObj.users, function (i, user) {
						items[user.id] = user.fullname + "(" + user.name + ")";
					});
					return items;
				}
			);
		})
		
		jQuery("#allextraInputer").ajaxChosen(
				{
					method: 'post',
					url: "${base}/teach/grade/lesson/input!searchUsers.action?pageNo=1&pageSize=10"
				}
				, function (data) {
					var items = {};
					var dataObj = eval("(" + data + ")");
					jQuery.each(dataObj.users, function (i, user) {
						items[user.id] = user.fullname + "(" + user.name + ")";
					});
					return items;
				}
			);
		[#--
		jQuery("#allextraInputer").change(function(){
			var allextraInputer = jQuery(this);
			jQuery("select[role='extraInputer']").each(function(i){
				jQuery(this).empty().append("<option value='"+allextraInputer.val()+"'>"+allextraInputer.find("option:selected").text()+"</option>").val(allextraInputer.val());
				jQuery("span","#extraInputer_"+i+"chosen").text(allextraInputer.find("option:selected").text());
			})
		})
		--]
	})

</script>
[@b.foot/]
