[#ftl/]
[#include "/template/macros.ftl"/]
[@b.div style="margin-top:10px;"/]
<table class="gridtable">
	<thead class="gridhead">
		<tr>
			<th>${b.text("attr.year2year")}</th>
			<th>${b.text("attr.term")}</th>
			<th>${b.text("std.grade.courseNumber")}</th>
			<th>${b.text("std.totalCredit")}</th>
			<th>${b.text("grade.avgPoints")}</th>
		</tr>
	</thead>
	<tbody>
			[#assign trClass="griddata-even"/]
			[#assign isDouble=false/]
			[#list stdGpa.semesterGpas as stdSemesterGpa]
					[#if isDouble]
						[#assign trClass="griddata-odd"/]
						[#assign isDouble=false/]
					[#else]
						[#assign trClass="griddata-even"/]
						[#assign isDouble=true/]
					[/#if]
					<tr class="${trClass}">
						<td>${(stdSemesterGpa.semester.schoolYear)!}</td>
						<td>${(stdSemesterGpa.semester.name)!}</td>
						<td>${(stdSemesterGpa.count)!}</td>
						<td>${(stdSemesterGpa.credits)!}</td>
						<td>${(stdSemesterGpa.gpa)!}</td>
					</tr>
		  	[/#list]
				[#if isDouble]
					[#assign trClass="griddata-odd"/]
					[#assign isDouble=false/]
				[#else]
					[#assign trClass="griddata-even"/]
					[#assign isDouble=true/]
				[/#if]
				<tr class="${trClass}">
						<th colSpan="2">${b.text("grade.schoolSummary")}</th>
						<th>${stdGpa.count!0}</th>
						<th>${stdGpa.credits!0}</th>
						<th>${stdGpa.gpa!0}</th>
				</tr>
		[#if isDouble]
			[#assign trClass="griddata-odd"/]
			[#assign isDouble=false/]
		[#else]
			[#assign trClass="griddata-even"/]
			[#assign isDouble=true/]
		[/#if]
	 	<tr class="${trClass}">
	 		<th colSpan="5" align="right">统计时间:${stdGpa.updatedAt?string("yyyy-MM-dd HH:mm")}</th>
		</tr>
	</tbody>
</table>

