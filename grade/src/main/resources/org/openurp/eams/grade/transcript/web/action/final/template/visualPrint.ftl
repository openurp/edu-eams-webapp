[#ftl]
<style type="text/css">
.reportTable {
    border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
    font-size: 10pt; 
    border-style: none;
    text-align:center;
}
table.reportTable td{
    border:solid;
    border-width:0px;
    border-color:#006CB2;
    border-left-width: 1px;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-right-width: 1px;
    font-size:12px;
}
</style>
[#include "/template/macros.ftl"/]
[#list stdGrades as stdGrade]
<h3 style="text-align:center;">[@i18nName school/]${b.text('common.studyGradeTable')}</h3>
<table width="95%" align="center" valign="top" style="font-size:11px">
	 <tbody>
	 	<tr>
	 		<td>${b.text('entity.department')}:[@i18nName stdGrade.std.department!/]</td>
	 		<td colspan="2">${b.text('entity.major')}(${b.text('entity.direction')}):[@i18nName stdGrade.std.major!/]</td>
	 		<td width="20%">${b.text('entity.adminClass')}:[@i18nName stdGrade.std.adminclass!/]</td>
	 		<td width="15%">${b.text('common.eduLength')}:${stdGrade.std.duration!}</td>
	 	</tr>
	 	<tr>
	 		<td>${b.text('attr.student.code')}:${stdGrade.std.code!}</td>
	 		<td>${b.text('attr.personName')}:[@i18nName stdGrade.std!/]</td>
	 		<td>${b.text('attr.gender')}:[@i18nName stdGrade.std.gender!/]</td>
	 		<td>${b.text('grade.creditTotal')}:${stdGrade.stdGpa.credits!}</td>
	 		<td>${b.text('filed.averageScoreNod')}:${stdGrade.stdGpa.gpa!}</td>
	 	</tr>
	 </tbody>
</table>
<table width="95%" align="center" class="reportTable" valign="top" style="font-size:11px">
	  <tbody>
	  	<tr align="center">
	  		[#list 1..2 as i]
		         <td width="23%" align="center">${b.text('field.teachAccident.courseName')}</td>
		         <td width="5%" align="center">${b.text('attr.credit')}</td>
		         <td width="7%">${b.text('grade.score')}</td>
		         <td width="5%">${b.text('filed.gradeNod')}</td>
		         <td width="5%">${b.text('field.teacherEvaluate.term')}</td>
		         <td width="5%">${b.text('field.teachProduct.remark')}</td>
	  		[/#list]
	  	</tr>
	  	[#assign courseGrades = stdGrade.grades!/]
	  	[#assign semesterMap = stdSemMap[stdGrade.std.code]/]
	  	[#if (courseGrades.size()/2-1)>0 || (courseGrades.size()/2-1)=0]
	  		[#list 0..(courseGrades.size()/2-1) as len]
	  			<tr>
	  			[#assign courseGradeFir = courseGrades[len*2]/]
	  			[#assign courseGradeSec = courseGrades[len*2+1]/]
	  			<td>[@i18nName courseGradeFir.course!/]</td>
	  			<td>${courseGradeFir.course.credits!}</td>
	  			<td>${courseGradeFir.scoreText!}</td>
	  			<td>${courseGradeFir.gp!}</td>
	  			<td>${semesterMap[(courseGradeFir.semester.id)+'']!}</td>
	  			<td>${courseGradeFir.remark!}</td>
	  			<td>[@i18nName courseGradeSec.course!/]</td>
	  			<td>${courseGradeSec.course.credits!}</td>
	  			<td>${courseGradeSec.scoreText!}</td>
	  			<td>${courseGradeSec.gp!}</td>
	  			<td>${semesterMap[(courseGradeSec.semester.id)+'']!}</td>
	  			<td>${courseGradeSec.remark!}</td>
	  			</tr>
	  		[/#list]
	  	[/#if]
	  	[#if courseGrades.size()%2!=0]
	  		[#assign courseGrade = courseGrades[courseGrades.size()-1]/]
		  		<tr>
		  			<td>[@i18nName courseGrade.course!/]</td>
		  			<td>${courseGrade.course.credits!}</td>
		  			<td>${courseGrade.scoreText!}</td>
		  			<td>${courseGrade.gp!}</td>
		  			<td>${semesterMap[(courseGrade.semester.id)+'']!}</td>
		  			<td>${courseGrade.remark!}</td>
		  			<td></td>
		  			<td></td>
		  			<td></td>
		  			<td></td>
		  			<td></td>
		  			<td></td>
		  		</tr>
	  	[/#if]
	  </tbody>
</table>	 
[@b.div style="margin-top:5px;"/]
<table width="95%" align="center" class="reportTable" valign="top" style="font-size:11px">
	<tbody>
		<tr align="center">
			<td width="20%" class="tableHeaderSort" id="otherGrade.category.name" name="entity.examType">${b.text('entity.examType')}</td>
         	<td width="20%">${b.text('attr.year2year')}</td>
         	<td width="15%">${b.text('attr.term')}</td>
         	<td width="20%">${b.text('grade.score')}</td>
   			<td class="tableHeaderSort" id="otherGrade.category.markStyle" name="entity.markStyle">${b.text('common.markStyle')}</td>
		</tr>
		[#assign otherGrades = otherGradeMap[stdGrade.std.code]/]
		[#list otherGrades as otherGrade]
			<tr>
				<td>${otherGrade.subject.name!}</td>
				<td>${otherGrade.semester.schoolYear!}</td>
				<td>${otherGrade.semester.name!}</td>
				<td>${otherGrade.score!}</td>
				<td>[@i18nName otherGrade.markStyle!/]</td>
			</tr>
		[/#list]
	</tboyd>
</table>
[@b.div style="margin-top:5px;"/]
<table width="95%" align="center" class="reportTable" valign="top" style="font-size:11px">
 	<tbody>
	 		<tr align="center" class="darkColumn">
	 			[#list 1..3 as i]
	 				<td name="common.teachCalendar">${b.text('common.semester')}</td>
					<td name="std.totalCredit">${b.text('std.totalCredit')}</td>
					<td name="grade.avgPoints">${b.text('filed.averageScoreNod')}</td>
	 			[/#list]
	 		</tr>
 			[#assign stdSemesterGpas = stdGrade.stdGpa.semesterGpas!/]
 			[#if (stdSemesterGpas.size()/3) !=0]
 				[#assign length = stdSemesterGpas.size()/3/]
 				[#if stdSemesterGpas.size()%3==0]
 					[#assign length = stdSemesterGpas.size()/3-1/]	
 				[/#if]
				[#list 0..length as len]
					<tr>
	 					[#list 0..2 as i]
	 						[#assign semesterGpa = stdSemesterGpas[len*3+i]!/]
		 						[#if stdSemesterGpas[len*3+i]??]
			 						<td>${semesterGpa.semester.schoolYear!}(${semesterGpa.semester.name!})</td>
			 						<td>${semesterGpa.credits!}</td>
			 						<td>${semesterGpa.gpa!}</td>
			 					[#else]
			 						<td></td><td></td><td></td>
		 						[/#if]
	 					[/#list]
				 	</tr>
				[/#list]
			[/#if]
 	</tbody>
 </table>
 <table width="95%" align="center" valign="top" style="font-size:11px">
        <tbody>
	       	<tr>
	            <td colspan="4">英文等级制与百分制对照规则：A(90-100) A-(85-89) B+(82-84) B(78-81) B-(75-77) C+(71-74) C(66-70) C-(62-65) D(60-61) D-(补考合格) F(0-59)</td>
	        </tr>
	        <tr>
	            <td colspan="4">*---学位课程</td>
	        </tr>
	        <tr>
	            <td colspan="4"></td>
	        </tr>
	        <tr>
	          <td width="25%">制表人：</td>
	          <td width="35%">学院（系）签章：&nbsp;</td>
	          <td width="20%">教务处签章：&nbsp;</td>
	          <td width="20%">${b.text('common.printDate')}:${setting.printAt?string('yyyy-MM-dd')}</td>
	       </tr>
    </tbody>
</table>
<div style="PAGE-BREAK-AFTER: always"></div>
[/#list]
