[#ftl /]
[@b.head/]
[#include "/template/macros.ftl"/]
[#include "/template/print.ftl"/]
<style>
td{
	font-size:12px;
}
</style>
[@b.toolbar title="教学质量分析表"]
  	bar.addPrint();
  	bar.addClose();
[/@]
    <br />
    <br />
    <br />
     <table width="95%" align="center" >
	   <tr>
	    <td align="center" colspan="3" style="font-size:17pt">
	    	 <B>${lesson.semester.schoolYear!} 第${lesson.semester.name!}学期 教学质量分析表</B>
	    </td>
	   </tr>
	   <tr><td>&nbsp;</td></tr>
	   <tr>
		   <td>任课老师：
		    [#list teachQuality.lesson.teachers?if_exists as teacher]
		   ${(teacher.name)!}&nbsp;&nbsp;
		   [/#list]
		  </td>
		   <td>所属学院：${(teachQuality.teacher.department.name)!}</td>
		   <td>任务编号：${(teachQuality.lesson.no)!}</td>
	   </tr>
	    <tr>
		   <td>课程名称：${(teachQuality.lesson.course.name)!}</td>
		   <td>开课学院：${(teachQuality.lesson.teachDepart.name)!}</td>
		   <td>考核方式：${(teachQuality.lesson.examMode.name)!}</td>
	   </tr>
	   <tr>
	   		<td colspan="3">所属教研室：_______________</td>
	   </tr>
	   </table>
	 <br/>
	 <br/>
	  
	 [#include "statTask.ftl"]
	 <table width="95%" align="center">
	 	<tr>
	 		<td>二. 教学质量分析：</td>
	 	</tr>
	 	<tr>
	 		<td>1.考试考查试卷分析：</td>
	 	</tr>
	 	<tr>
	 		<td height="100px">${(teachQuality.test)!}</td>
	 	</tr>
	 	<tr>
	 		<td>2.学生学习情况和答题情况分析：</td>
	 	</tr>
	 	<tr>
	 		<td height="100px">${(teachQuality.stdLearn)!}</td>
	 	</tr>
	 	<tr>
	 		<td>3.本人教学工作情况分析：</td>
	 	</tr>
	 	<tr>
	 		<td height="100px">${(teachQuality.teach)!}</td>
	 	</tr>
	 </table>
	 <table width="95%" align="center">
	 	<tr>
	 		<td>三. 教学的经验教训，改进措施与建议：</td>
	 	</tr>
	 	<tr>
	 		<td height="100px">${(teachQuality.teachExperience)!}</td>
	 	</tr>
	 </table>
	 <table width="95%" align="center">
	 	<tr>
	 		<td>四. 教研室（系）意见：</td>
	 	</tr>
	 	<tr>
	 		<td height="100px"></td>
	 	</tr>
	 </table>
	 <table width="95%" align="center">
	 	<tr>
	 		<td align="right">教研室（系）主任签名_________________日期________________</td>
	 	</tr>
	 </table>
[@b.foot/]