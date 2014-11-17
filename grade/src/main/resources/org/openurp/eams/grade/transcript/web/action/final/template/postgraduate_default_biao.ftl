[#ftl/]
[@b.head /]
[@b.toolbar]
bar.addPrint();
bar.addClose();
[/@]
<style>
.zgPrintTable { 
	border : 1px solid #000;
	border-collapse : collapse;
	width : 700px; font-size:16px
}
.zgPrintTable td {
	border  : 1px solid #000;
}
</style>
[@b.form name="postgraduateForm" target="_bank"]
		<input type="hidden" name="configId" id="configId" />
		<input type="hidden" name="params" value="[@htm.queryStr /]" />
[/@]
[#function getCredits grades]
	[#assign credits = 0 /]
	[#list grades as grade]
		[#assign credits = (credits + grade.course.credits)/]
	[/#list]
	[#return credits /]
[/#function]

[#function getResult grades]
	[#assign result = 0 /]
	[#list grades as grade]
		[#assign result = result + grade.score /]
	[/#list]
	[#return result /]
[/#function]

[#function getTeacher grades]
	[#assign result ="" /]
	[#list grades as grade]
	   [#list grade.lesson.teachers as teacher]
		[#assign result = teacher.name /]
	   [/#list]
	[/#list]
	[#return result /]
[/#function]

[#function getCredit planCourses]
	[#assign credits = 0 /]
	[#list planCourses as grade]
		[#assign credits = grade.courseGroup.getPlan().getCredits() /]
	[/#list]
	[#return credits /]
[/#function]
        [#list students as std]
<table align="center" class="zgPrintTable">
        <tr align="center">
		    <td colspan="7"><h2>同济大学研究生课程成绩单</h2></td>
	    </tr> 
	    <tr>
	        <td aling="center">攻读学位类别：</td><td colspan="4">${(std.getMajor().degree.name)!}</td><td>打印日期：</td><td>${date?string("yyyy-MM-dd")}</td>
	    </tr>
	    <tr align="center">
	        <td >学号</td><td>${(std.code)!}</td><td>姓名</td><td colspan="2">${(std.name)!}</td><td>性别</td><td>${(std.gender.name)!}</td>
	    </tr>
	    <tr align="center">
	        <td>专业</td><td colspan="2">${(std.getMajor().name)!}</td> <td colspan="2">入学年月</td><td colspan="2">${(std.registOn?string('yyyy年MM月'))!}</td>
	    </tr>
	    <tr align="center">
	        <td>所在学院</td><td colspan="2">${(std.majorDepart.name)!}</td> <td colspan="2">指导教师</td><td colspan="2">${getTeacher(grades.get(std))!}</td>
	    </tr>
	    <tr align="center">
	        <td>应修学分</td><td colspan="2"></td> <td colspan="2">已修学分</td><td colspan="2"> ${getCredits(grades.get(std))!}</td>
	    </tr>
	    <tr align="center">
	    <td>课程类别</td><td colspan="2">课程名称</td><td>学分</td><td>成绩</td><td>上课学期</td><td>备注</td>
	    </tr>
	        [#list grades.get(std) as grade]
	    <tr align="center">
	    <td>${(grade.lesson.courseType.name)!}</td><td colspan="2">${(grade.getCourse().getName())!}</td><td>${(grade.getCourse().credits)!}</td><td>${(grade.score)!}</td><td>${(grade.lesson.semester.beginOn?string("yyyy-MM"))!}</td><td>${(grade.remark)!}</td>
	    </tr>
	         [/#list]
	    <tr>
	    <tr align="center">
	    <td>总学分</td><td> ${getCredits(grades.get(std))!}</td><td>学位课总学分</td><td colspan="2">${getCredit(planCourses.get(std))}</td><td>学位课平均成绩</td><td>${getResult(grades.get(std))}</td>
	    </tr >
	    <tr style= "font-size:16px ">
	    <td>教务员（签章）</td><td colspan="2"></td><td>单位主管领导（签章）：</td><td colspan="2"><td>研究生院（盖章）</td>
	    </tr>
</table><br/><br/>	
        [#if std_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]    
	    [/#list]		    
	    
[@b.foot/]