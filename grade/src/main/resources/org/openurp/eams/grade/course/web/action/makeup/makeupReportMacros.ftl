[#ftl]
[#macro makeupReportHead]
     <table width="100%" align="center" border="0"  >
	   <tr>
	    <td align="center" colspan="5" style="font-size:17pt" >
	     <B>${project.school.name}补缓考成绩</B>
	    </td>
	   </tr>
	   <tr><td align="center" colspan="5" style="font-size:17pt">(${semester.schoolYear}学年 [#if semester.name='1']第一学期[#elseif semester.name='2']第二学期[#else]${semester.name}[/#if])</td></tr>
	   <tr><td colspan="5">&nbsp;</td></tr>
	 </table>
	 <table width="100%" align="center" border="0"  >
	 <tr class="infoTitle">
	       <td >${b.text('attr.courseNo')}：${course.code}</td>
		   <td >${b.text('attr.courseName')}：[@i18nName course?if_exists/]</td>
		   <td >${b.text('attr.teachDepart')}：[@i18nName teachDepart/]</td>
		   <td >考试学生：${examTakeList?size}</td>
		   <td >学分：${course.credits}</td>
	 </tr>
	 </table>
[/#macro]

[#macro makeupReportFoot]
     <table  width="100%"  align="center">
        <tr>
            <td width='25%'>阅卷教师签名:</td><td width='25%'>日期:</td>
            <td width='25%'>成绩登录者签名:</td></td><td width='25%'>日期:</td>
        </tr>
        <tr><td></td></tr><tr><td></td></tr>
        <tr><td width='25%'>专业负责人签名:</td><td width='25%'>日期:</td></tr>  
     </table>
[/#macro]
[#include "makeupReportMacroExt.ftl"]