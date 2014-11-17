
<#macro majorSelect id projects extra...>
    <script src='${base}/dwr/interface/semesterDao.js'></script>
    <script src='${base}/dwr/interface/projectMultiSelectForESD.js'></script>
    <script src='${base}/dwr/interface/projectMajorSelect.js'></script>
    <script src='${base}/static/scripts/common/SemesterWithProjectRefAll.js'></script>
	<tr <#if (!extra["isDisplayProject"]?exists || extra["isDisplayProject"] != "1") && (projects?size==1)>style="display:none;"</#if>>
		<td>项目:</td>
		<td>
			<select id="${id}project"  name="${extra['projectId']!'project.id'}" style="width:100%;">
				<#if extra['defaultProjectId']??>
				<option value="${extra['defaultProjectId']}"><@text name="filed.choose"/>...</option>
				<#else>
				<option value=""><@text name="filed.choose"/>...</option>
				</#if>
			</select>
		</td>
	</tr>
	<#if extra['educationId']??>
	<tr>
		<td><@text name="education"/>:</td>
		<td>
			<select id="${id}education"  name="${extra['educationId']!'education.id'}" style="width:100%;">
				<option value=""><@text name="filed.choose"/>...</option>
			</select>
		</td>
	</tr>
	</#if>
	<#if extra['stdTypeId']??>
	<tr>
		<td><@text name="entity.studentType"/>:</td>
		<td>
			<select id="${id}stdType" name="${extra['stdTypeId']}" style="width:100%;">
				<option value=""><@text name="filed.choose"/>...</option>
			</select>
		</td>
	</tr>
	</#if>
	<#if extra['departmentId']??>
	<tr>
		<td><@text name="common.college"/>:</td>
		<td>
			<select id="${id}department" name="${extra['departmentId']}" style="width:100%;">
				<option value=""><@text name="filed.choose"/>...</option>
			</select>
		</td>
	</tr>
	</#if>
	<#if extra['majorId']??>
	<tr>
		<td><@text name="entity.major"/>:</td>
		<td>
			<select id="${id}major" name="${extra['majorId']}" style="width:100%;">
				<option value=""><@text name="filed.choose"/>...</option>
			</select>
		</td>
	</tr>
	</#if>
	<#if extra['directionId']??>
	<tr>
		<td><@text name="entity.direction"/>:</td>
		<td>
			<select id="${id}direction" name="${extra['directionId']}" style="width:100%;">
				<option value=""><@text name="filed.choose"/>...</option>
			</select>
		</td>
	</tr>
	</#if>
	<#if extra['year']??>
    <tr>
        <td><@text name="attr.year2year"/>:</td>
        <td>
            <div style="display:none">
            <select id="academicCalendar" name="semester.calendar.id" style="width:100%;">
            <option value="${semester.calendar.id}"></option>
           </select>
           </div>
            <select id="year" name="courseGrade.semester.schoolYear" style="width:100%;">
            </select>
        </td>
    </tr>
    </#if>
    <#if extra['term']??>
    <tr>
        <td><@text name="attr.term"/>:</td>
        <td>
            <select id="term" name="courseGrade.semester.name" style="width:100%;">
            </select>
       </td>
    </tr>
    </#if>
    <script>
    	var ${id}projectArray = new Array();
    	<#list projects as project>
    	${id}projectArray[${project_index}]={'id':'${project.id}','name':'${project.name}'};
    	</#list>
    	<#if !(projectNullable?exists)>
    		<#assign projectNullable=false/>
    	</#if>
    	<#if !(educationNullable?exists)>
    		<#assign educationNullable=false/>
    	</#if>
    	<#if !(departmentNullable?exists)>
    		<#assign departmentNullable=false/>
    	</#if>
    	<#if !(studentTypeNullable?exists)>
    		<#assign studentTypeNullable=false/>
    	</#if>
    	<#if !(majorNullable?exists)>
    		<#assign majorNullable=false/>
    	</#if>
    	<#if !(directionNullable?exists)>
    		<#assign directionNullable=false/>
    	</#if>
    	<#if !(yearNullable?exists)>
    		<#assign yearNullable=false/>
    	</#if>
    	<#if !(termNullable?exists)>
    		<#assign termNullable=false/>
    	</#if>
    	<#if !(projectDefaultFirst?exists)>
    		<#assign projectDefaultFirst=true/>
    	</#if>
    	var ${id}sds = new SemesterSelect("${id}project",${extra['year']?exists?string(id + "\"year\"", "null")},${extra['term']?exists?string(id + "\"term\"", "null")},${extra['educationId']?exists?string(id + "\"education\"", "null")},${extra['departmentId']?exists?string(id + "\"department\"", "null")},${extra['stdTypeId']?exists?string(id + "\"stdType\"", "null")},${extra['majorId']?exists?string(id + "\"major\"", "null")},${extra['directionId']?exists?string(id + "\"direction\"", "null")},${projectNullable?string},${educationNullable?string},${departmentNullable?string},${studentTypeNullable?string},${majorNullable?string},${directionNullable?string},${yearNullable?string},${termNullable?string},${projectDefaultFirst?string});
    	${id}sds.init(${id}projectArray);
    </script>
</#macro>
