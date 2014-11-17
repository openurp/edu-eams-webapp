[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[#macro emptyTd count]
     [#list 1..count as i]
     <td></td>
     [/#list]
[/#macro]
[@b.toolbar title="补缓考成绩录入"]
	bar.addBack("${b.text("action.back")}");
[/@]
<script>
	function checkScore(ele){
		var maxScore=100;
		var score=ele.value;
		if(null==score ||""==score) return;
		if(!(/^\d*\.?\d{1}$/.test(score))){alert("请输入数字");ele.value='';return;}
		scoreInt = parseInt(score);
		if(-1!=document.getElementById(ele.id+"examType").innerHTML.indexOf("补")){
			maxScore=60;
		}
		if(scoreInt>maxScore) {alert(score+"超出了"+maxScore);ele.value="";}
	}
</script>
     <table width="80%" align="center" border="0"  >
	   <tr>
	    <td align="center" colspan="5" style="font-size:16px" >
	     <B>${course.name} 补缓考成绩录入</B>
	    </td>
	   </tr>
	   <tr><td colspan="5">&nbsp;</td></tr>
	 </table>	 
	 
	 <table width="80%" align="center" border="0"  >
		 <tr class="infoTitle">
		       <td >学年学期:${semester.schoolYear}学年 [#if semester.name='1']第一学期[#elseif semester.name='2']第二学期[#else]${semester.name}[/#if]</td>
			   <td >${b.text("attr.courseNo")}:${course.code}</td>
			   <td >${b.text("attr.courseName")}:[@i18nName course?if_exists/]</td>
			   <td >${b.text("attr.teachDepart")}:[@i18nName teachDepart/]</td>
			   <td >考试学生:${examTakeList?size}</td>
		 </tr>
	 </table>
	 [@b.form name="actionForm" action="!batchSaveCourseGrade"]
		 <input type="hidden"  name="makeupCourse.id" value="${course.id}@${teachDepart.id}" />
		 <input type="hidden" id="semester.id" name="semester.id" value="${semester.id}" />
		 
		 <table class="gridtable" style="width:95%" align="center" >
		   <tr class="gridhead">
		     <td width="5%" >序号</td>
		     <td width="15%">${b.text("attr.stdNo")}</td>
		     <td width="10%">${b.text("attr.personName")}</td>	     
		     <td width="10%">考试类型</td>
		     <td width="10%">考试成绩</td>
		     <td width="5%">序号</td>
		     <td width="15%">${b.text("attr.stdNo")}</td>
		     <td width="10%">${b.text("attr.personName")}</td>
		     <td width="10%">考试类型</td>
		     <td width="10%">考试成绩</td>
		   </tr>
		   [#assign examTakeList=examTakeList?sort_by(['std','code'])]
	       [#assign pageNo=(((examTakeList?size-1)/2)?int)  /]
		   [#list 0..pageNo as i]
		   <tr class="brightStyle" style="text-align:center">
	         [#if examTakeList[i]?exists]
		     <td>${i+1}</td>
		     <td>${examTakeList[i]?if_exists.std?if_exists.code}</td>
		     <td>[@i18nName examTakeList[i]?if_exists.std?if_exists/]</td>
		     <td id="${examTakeList[i].id}examType">[@i18nName examTakeList[i]?if_exists.examType/]</td>
		     [#assign examGradeNo=examTakeList[i].id?string  /]
		     <td>[#if ((examGradeMap[examGradeNo].status)!0)=2]${examGradeMap[examGradeNo].scoreText!}<sup>已发布</sup>[#else]
		     <input id="${examTakeList[i].id}" maxlength="3" onchange="checkScore(this)" name="${examTakeList[i].id}" TABINDEX="${i+1}" value="${(examGradeMap[examGradeNo].score)!}"  style="width:60px"/>[/#if]
		     </td>
	         [/#if]
	         [#assign nextExam=i+pageNo+1]
	         [#if examTakeList[nextExam]?exists]
		     <td>${nextExam+1}</td>
		     <td>${examTakeList[nextExam].std?if_exists.code}</td>
		     <td>[@i18nName examTakeList[nextExam].std?if_exists/]</td>
		     <td>[@i18nName examTakeList[nextExam].examType/]</td>
			 [#assign examGradeNo=examTakeList[nextExam].id?string  /]
		     <td>[#if ((examGradeMap[examGradeNo].status)!0)=2]${examGradeMap[examGradeNo].scoreText!}<sup>已发布</sup>[#else]
		     <input id="${examTakeList[nextExam].id}" maxlength="3"  onchange="checkScore(this)" name="${examTakeList[nextExam].id}"  TABINDEX="${nextExam+1}" value="${(examGradeMap[examGradeNo].score)!}" style="width:60px"/>[/#if]</td>
	         [#else]
	          [@emptyTd count=5/]
	         [/#if]
	   </tr>
	   [/#list] 
	   <tr>
			<td align="center" colspan="10">请选择成绩录入后的状态: 
		      [@b.select name="grade.state" style="width:100px" items={'1':'确认','0':'新添加','2':'发布'}/]
		      [@b.submit value="提交"/]
		   	</td>
	   </tr>
     </table>
     [/@]
[@b.foot/]
