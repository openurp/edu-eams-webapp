<#macro majorSelect id projects extra...>
	<tr [#if (!extra["isDisplayProject"]?exists || extra["isDisplayProject"] == "1") && (projects?size==1)]style="display:none;"[/#if]>
		<td>项目:</td>
		<td>
			<select id="${id}project"  name="${extra['projectId']!'project.id'}" style="width:100%;">
				[#if extra['defaultProjectId']??]
				<option value="${extra['defaultProjectId']}">${b.text("filed.choose")}...</option>
				[#else]
				<option value="">${b.text("filed.choose")}...</option>
				[/#if]
			</select>
		</td>
	</tr>
	<tr>
		<td>${b.text("education")}:</td>
		<td>
			<select id="${id}education"  name="${extra['educationId']!'education.id'}" style="width:100%;">
				<option value="">${b.text("filed.choose")}...</option>
			</select>
		</td>
	</tr>
	[#if extra['stdTypeId']??>
	<tr>
		<td>${b.text("entity.studentType")}:</td>
		<td>
			<select id="${id}stdType" name="${extra['stdTypeId']}" style="width:100%;">
				<option value="">${b.text("filed.choose")}...</option>
			</select>
		</td>
	</tr>
	[/#if]
	<tr>
		<td>${text("common.college")}:</td>
		<td>
			<select id="${id}department" name="${extra['departId']}" style="width:100%;">
				<option value="">${b.text("filed.choose")}...</option>
			</select>
		</td>
	</tr>
	[#if extra['majorId']??>
	<tr>
		<td>${b.text("entity.major")}:</td>
		<td>
			<select id="${id}major" name="${extra['majorId']}" style="width:100%;">
				<option value="">${b.text("filed.choose")}...</option>
			</select>
		</td>
	</tr>
	[/#if]
	[#if extra['directionId']??>
	<tr>
		<td>${b.text("entity.direction")}:</td>
		<td>
			<select id="${id}direction" name="${extra['directionId']}" style="width:100%;">
				<option value="">${b.text("filed.choose")}...</option>
			</select>
		</td>
	</tr>
	[/#if]
<script src='${base}/dwr/interface/projectMajorSelect.js'></script>
<script src='${base}/static/scripts/common/major3Select.js'></script>
<script>
	var ${id}projectArray = new Array();
	[#list projects as project]
	${id}projectArray[${project_index}]={'id':'${project.id}','name':'${project.name}'};
	[/#list]
	[#if !(educationNullable?exists)>
		[#assign educationNullable=false]
	[/#if]
	var ${id}sds = new Major3Select("${id}project","${id}education",[#if extra['stdTypeId']??>"${id}stdType"[#else]null[/#if],"${id}department",[#if extra['majorId']??]"${id}major"[#else]null[/#if],[#if extra['directionId']??]"${id}direction"[#else]null[/#if],true,true,true,true);    
	${id}sds.init(${id}projectArray);
</script>
</#macro>