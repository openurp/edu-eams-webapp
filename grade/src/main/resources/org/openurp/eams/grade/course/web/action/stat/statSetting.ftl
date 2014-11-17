[#ftl]
[@b.form name="statSettingForm"  target="_blank"]
	<input type="hidden" name="courseId" value="${courseId}"/>
[/@]
<table>
	<thead>
		<th>学期列表</th>
		<th></th>
		<th>已选列表</th>
	</thead>
	<tbody>
		<tr>
			<td>
				<select ondblclick="JavaScript:bg.select.moveSelected(jQuery('#allSemester')[0],jQuery('#selectSemester')[0])" style="width:200px;height:200px" size="15" multiple="multiple" id="allSemester">
					[#list semesters as semester]
						<option value="${semester.id}">${semester.schoolYear}学年第${semester.name}学期</option>
					[/#list]
				</select>
			</td>
			<td style="width:30px">
				<input type="button" value="&gt;" onclick="JavaScript:bg.select.moveSelected(jQuery('#allSemester')[0],jQuery('#selectSemester')[0])" style="margin:auto">
				<input type="button" value="&lt;" onclick="JavaScript:bg.select.moveSelected(jQuery('#selectSemester')[0], jQuery('#allSemester')[0])" style="vertical-align: middle;">
			</td>
			<td>
				<select ondblclick="JavaScript:bg.select.moveSelected(jQuery('#selectSemester')[0], jQuery('#allSemester')[0])" style="width:200px;height:200px" size="15" multiple="multiple" id="selectSemester">
				</select>
			</td>
		</tr>
	</tbody>
	<tfoot>
		<td colspan="3" style="padding-top:20px;text-align:center">
			<input type="button" style="width:70px;" onclick="stat(document.statSettingForm)" value="统计"/>&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="button" style="width:70px;" onclick="jQuery.colorbox.close()" value="取 消"/>
		</td>
	</tfoot>
</table>
<script type="text/javascript">
	function stat(form){
		var selectSemesterObj = document.getElementById('selectSemester');
		var semesterIds = '';
		for(var i = 0 ; i<selectSemesterObj.options.length;i++){
			semesterIds = semesterIds+(selectSemesterObj.options)[i].value+",";
		}
		if('' == semesterIds){
			alert('请选择一个或多个日历!');
			return false;
		}
		bg.form.addInput(form,"semesterIds",semesterIds);
		bg.form.submit(form,"${b.url('stat!stat')}");
		jQuery.colorbox.close();
	}
</script>