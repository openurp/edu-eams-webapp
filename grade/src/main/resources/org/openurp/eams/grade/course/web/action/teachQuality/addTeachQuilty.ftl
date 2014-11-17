[#ftl /]
[@b.head/]
<style>
td{
	font-size:12px;
}
</style>
    <br />
    <br />
    <br />
    <form method="post" name="actionForm" target="contentDiv">
     <table width="85%" align="center" >
	   <tr>
	    <td align="center" colspan="3" style="font-size:17pt">
	    	 <B>${lesson.semester.schoolYear!} 第${lesson.semester.name!}学期 教学质量分析表</B>
	    </td>
	   </tr>
	   <tr><td>&nbsp;</td></tr>
	   <tr>
		   <td>所属学院：${(teacher.department.name)!}</td>
		   <td>任课老师：
		   [#list lesson.teachers?if_exists as teacher]
		   ${(teacher.name)!}&nbsp;&nbsp;
		   [/#list]
		   </td>
		   <td>任务编号：${(lesson.no)!}
		   <input type="hidden" name="teachQId" value="${(teachQ.id)!}" />
		    	<input type="hidden" name="lesson.id" value="${(lesson.id)!}" />
		    	<input type="hidden" name="teacher.id" value="${(teacher.id)!}" />
		    	<input type="hidden" name="semester.id" value="${(lesson.semester.id)!}" />
		   </td>
	   </tr>
	    <tr>
		   <td>课程名称：${(lesson.course.name)!}</td>
		   <td>开课学院：${(lesson.course.department.name)!}</td>
		   <td>考核方式：${(lesson.course.examMode.name)!}</td>
	   </tr>
	   <tr>
	   		<td colspan="3">所属教研室：_________________</td>
	   </tr>
	  </table>
	  <br />
	 [#include "statTask.ftl"]
	 <br />
	 <table width="85%" align="center">
	 	<tr>
	 		<td>二. 教学质量分析：<font color="red">（提示：如不在网上提交，可打印模板，在纸质中填写提交。）</font></td>
	 	</tr>
	 	<tr>
	 		<td>1.考试考查试卷分析：<font color="red">（提示：课程大纲的吻合程度，题目难易程度。）</font></td>
	 	</tr>
	 	<tr>
	 		<td><textarea name="teachQuality.test" id="teachQuality.test" maxlength="300" style="width:90%;height:75px;resize: none;">${teachQ.test!}</textarea><font color="red">（300字）</font></td>
	 	</tr>
	 	<tr>
	 		<td>2.学生学习情况和答题情况分析：<font color="red">（提示：含出勤和作业完成情况，学生知识点掌握程度。）</font></td>
	 	</tr>
	 	<tr>
	 		<td><textarea name="teachQuality.stdLearn" id="teachQuality.stdLearn" maxlength="300" style="width:90%;height:75px;resize: none;">${teachQ.stdLearn!}</textarea><font color="red">（300字）</font></td>
	 	</tr>
	 	<tr>
	 		<td>3.本人教学工作情况分析：<font color="red">（提示：含平时作业批改，对学生考勤情况。）</font></td>
	 	</tr>
	 	<tr>
	 		<td><textarea  name="teachQuality.teach" id="teachQuality.teach" maxlength="300" style="width:90%;height:75px;resize: none;">${teachQ.teach!}</textarea><font color="red">（300字）</font></td>
	 	</tr>
	 </table>
	 <br/>
	 <table width="85%" align="center">
	 	<tr>
	 		<td>三. 教学的经验教训，改进措施与建议：</td>
	 	</tr>
	 	<tr>
	 		<td><textarea name="teachQuality.teachExperience" id="teachQuality.teachExperience" maxlength="300" style="width:90%;height:75px;resize: none;">${teachQ.teachExperience!}</textarea><font color="red">（300字）</font></td>
	 	</tr>
	 </table>
	 <table width="85%" align="center">
	 	<tr>
	 		<td align="center">
	 			<input type="button" onclick="subs(document.actionForm)" value="提交" />
	 			<input type="button" onclick="javascript:window.close();" value="关闭" />
	 		</td>
	 	</tr>
	 </table>
  </form>
  <script type="text/javascript">
	function subs(form) {
        var str1 = document.getElementById("teachQuality.test").value;
	  	var str2 = document.getElementById("teachQuality.stdLearn").value;
	  	var str3 = document.getElementById("teachQuality.teach").value;
	  	var str4 = document.getElementById("teachQuality.teachExperience").value;
	  	var str = "";
	  	var num = 0;
	  	if(str1 == "" && str2 == "" && str3 == "" && str4 == ""){
	  		if(confirm("请确认走纸质流程？\n")){
	  		num =1;
	  		}else{
			return false;	  		
	  		}
	  	}else{
	  	if(str1 != "" && str2 != "" && str3 != "" && str4 != ""){
	  	if(str1.length >300){
	  		str += "1.考试考查试卷分析 的内容不可超过240字！"+"\n";
	  		}
	  		if(str2.length >300){
	  		str += "2.学生学习情况和答题情况分析 的内容不可超过240字！"+"\n";
	  		}
	  		if(str3.length >300){
	  		str += "3.本人教学工作情况分析 的内容不可超过240字！"+"\n";
	  		}
	  		if(str4.length >300){
	  		str += "三．教学的经验教训，改进措施与建议  的内容不可超过240字！"+"\n";
	  		}
	  		
	  	}else if(str1 == "" || str2 == "" || str3 == "" || str4 == ""){
	  		str += "请填写完整！"+"\n";
	  	}
	  	}
	  	if(str !=""){
	  		alert(str);
	  		return false;
	  	}else{
			[#include "../components/segScore.ftl"/]
			[@addSeqToForm 'form'/]
			[@addSeqToParams 'form'/]
	   		form.action="teach-quality!saveTeachQuilty.action?flow="+num;
	   		form.submit();
		}
		
	}
</script>
  
[@b.foot/]