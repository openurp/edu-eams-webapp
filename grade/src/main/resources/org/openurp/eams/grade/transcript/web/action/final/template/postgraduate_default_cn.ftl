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
		[#assign credits = credits + grade.course.credits /]
	[/#list]
	[#return credits /]
[/#function]
[#function getCredit planCourses]
	[#assign credits = 0 /]
	[#list planCourses as grade]
		[#assign credits = grade.courseGroup.getPlan().getCredits() /]
	[/#list]
	[#return credits /]
[/#function]
[#function getCompulsory planCourses]
	[#assign compulsory = "" /]
	[#list planCourses as grade]
		[#assign compulsory = grade.isCompulsory()?string("√","") /]
	[/#list]
	[#return compulsory /]
[/#function]
[#function notCompulsory planCourses]
	[#assign compulsory = "" /]
	[#list planCourses as grade]
		[#assign compulsory = grade.isCompulsory()?string("","√") /]
	[/#list]
	[#return compulsory /]
[/#function]
         [#list students as std]
<table align="center" class="zgPrintTable">
        <tr align="center">
		    <td colspan="9"><h1>同济大学研究生课程成绩表</h1></td>
	    </tr> 
	    <tr align="center">
	        <td colspan="2">攻读学位：</td><td colspan="4">${(std.getMajor().degree.name)!}</td><td colspan="2">打印日期：</td><td>${date?string("yyyy-MM-dd")}</td>
	    </tr>
	    <tr align="center">
	        <td colspan="2">姓名</td><td colspan="2">${(std.name)!}</td><td colspan="2">学院（系、所）</td><td colspan="3">${(std.majorDepart.name)!}</td>
	    </tr>
	    <tr align="center">
	        <td colspan="2">学号</td><td colspan="2">${(std.code)!}</td><td colspan="2">专业</td><td colspan="3">${(std.getMajor().name)!}</td>
	    </tr>
	    <tr align="center">
	        <td colspan="2">学制</td><td colspan="2">${(std.duration)!}</td><td colspan="2">研究方向</td><td colspan="3">${(std.direction.name)!}</td>
	    </tr>
	    <tr align="center">
	        <td>${(std.registOn?string('yyyy'))!}</td><td align="center">年</td><td align="center">${(std.registOn?string('MM'))!}</td><td align="center">月入学</td><td align="center">规定修满</td><td align="center">${getCredit(planCourses.get(std))}</td><td colspan="3">学分</td>
	    </tr>
	    <tr align="center">
	        <td align="center">${(std.graduateOn?string('yyyy'))!}</td><td align="center">年</td><td align="center">${(std.graduateOn?string('MM'))!}</td><td align="center">月毕业</td><td align="center">实际修得</td><td align="center"> ${getCredits(grades.get(std))}</td><td colspan="3">学分</td>
	    </tr>
	    <tr align="center">
	        <td colspan="4">科目</td><td>成绩</td><td>学分</td><td>必修</td><td>选修</td><td>备注</td>
	    </tr>
	    [#list grades.get(std) as grade]
	     <tr align="center">
	        <td colspan="4">${(grade.course.name)!}</td><td>${(grade.score)!}</td><td>${(grade.course.credits)}</td><td>${getCompulsory(planCourses.get(std))}</td><td>${notCompulsory(planCourses.get(std))}</td><td>${(grade.course.remark)!}</td>
	    </tr>
	    [/#list]
	     <tr style= "font-size:25px ">
	    <td>填表人</td><td colspan="2"></td><td colspan="2">单位主管领导</td><td></td><td colspan="3">研究生院（公章）</td>
	    </tr>
</table><br/><br/>	
        [#if std_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]    
	    [/#list]
[@b.foot/]