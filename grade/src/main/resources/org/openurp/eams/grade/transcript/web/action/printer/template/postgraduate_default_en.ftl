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
		    <td colspan="6"><h2>TongJi University Graduate Student’s Academic Record</h2></td>
	    </tr> 
	    <tr>
	        <td>Degree:</td><td colspan="5">${(std.getMajor().degree.engName)!}</td>
	    </tr>
	    <tr align="center">
	        <td>Name</td><td>${(std.engName)!}</td><td colspan="2">Department(Institute)</td><td colspan="2">${(std.department.engName)!}</td>
	    </tr>
	    <tr align="center">
	        <td>Student Identification</td><td>${(std.code)!}</td><td colspan="2">Major</td><td colspan="2">${(std.major.engName)!}</td>
	    </tr>
	    <tr align="center">
	        <td>Length of Program</td><td>${(std.duration)!}</td><td colspan="2">Area of research</td><td colspan="2">${(std.direction.name)!}</td>
	    </tr>
	    <tr>
	        <td align="center">Date   of    Enrollment:</td><td>${(std.registOn?string('MM,yyyy'))!}</td><td colspan="3" align="center">Date    of    Graduation:</td><td>${(std.graduateOn?string('MM,yyyy'))!}</td>
	    </tr>
	    <tr align="center">
	        <td >Course</td><td>Score</td><td>Credit</td><td>Required Course</td><td>Elective Course</td><td>Remarks</td>
	    </tr>
	    [#list grades.get(std) as grade]
	     <tr align="center">
	        <td>${(grade.getCourse().getEngName())!}</td><td> ${(grade.score)!}</td><td>${(grade.getCourse().credits)!}</td><td>${getCompulsory(planCourses.get(std))}</td><td>${notCompulsory(planCourses.get(std))}</td><td>${(grade.course.remark?html)!}</td>
	    </tr>
	    [/#list]
	     <tr>
	    <td colspan="6"><h3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Writer:  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Department (Institute) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Chairman:TongJi University</h3> </td>
	    </tr>
</table><br/><br/>	
        [#if std_has_next]<div style='PAGE-BREAK-AFTER: always'></div>[/#if]    
	    [/#list]	    
	    
[@b.foot/]