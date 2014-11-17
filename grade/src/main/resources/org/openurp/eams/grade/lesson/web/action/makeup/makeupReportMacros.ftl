[#ftl]
[#macro makeupReportHead]
     <table width="100%" align="center" border="0"  >
	   <tr>
	    <td align="center" colspan="5" style="font-size:17pt" >
	     <B>${project.school.name}${b.text('grade.slowUpResultsManagement')}</B>
	    </td>
	   </tr>
	   <tr><td align="center" colspan="5" style="font-size:17pt">(${semester.schoolYear}${b.text("grade.course.lessonReport.AcademicYear")} [#if semester.name='1']${b.text("grade.theFirstSemester")}[#elseif semester.name='2']${b.text("grade.tedSecondSemester")}[#else]${semester.name}[/#if])</td></tr>
	   <tr><td colspan="5">&nbsp;</td></tr>
	 </table>
	 <table width="100%" align="center" border="0"  >
	 <tr class="infoTitle">
	       <td >${b.text('attr.courseNo')}：${course.code}</td>
		   <td >${b.text('attr.courseName')}：[@i18nName course?if_exists/]</td>
		   <td >${b.text('attr.teachDepart')}：[@i18nName teachDepart/]</td>
		   <td >${b.text("grade.course.makeup.ExamStudents")}：${examTakeList?size}</td>
		   <td >${b.text("attr.credit")}：${course.credits}</td>
	 </tr>
	 </table>
[/#macro]

[#macro makeupReportFoot]
     <table  width="100%"  align="center">
        <tr>
            <td width='25%'>${b.text("grade.course.makeup.MarkingTeacherSignature")}:</td><td width='25%'>${b.text("common.date")}:</td>
            <td width='25%'>${b.text("grade.course.makeup.TheAchievementsLoginSignature")}:</td></td><td width='25%'>${b.text("common.date")}:</td>
        </tr>
        <tr><td></td></tr><tr><td></td></tr>
        <tr><td width='25%'>${b.text("grade.course.makeup.ProfessionalResponsibleForTheSignature")}:</td><td width='25%'>${b.text("common.date")}:</td></tr>  
     </table>
[/#macro]
[#include "makeupReportMacroExt.ftl"]