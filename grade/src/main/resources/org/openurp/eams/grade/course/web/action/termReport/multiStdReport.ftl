[#ftl/]
[@b.head/]
[#include "/template/print.ftl"/]
[@b.toolbar title="学生每学期成绩"]
[#if Parameters['notprint']??]
	[#if !(semesters?? && semesters?size>1) ]
	bar.addItem("查看${semester.schoolYear}学年成绩","schoolYearGrade()");
	[/#if]
	bar.addItem("生成打印版","preview()");
	function preview(){
		window.open('${b.url("!index?adminclass.id=${Parameters['adminclass.id']}&semester.id=${Parameters['semester.id']}")}');
	}
	function schoolYearGrade(){
		window.open('${b.url("!index?adminclass.id=${adminclass.id}&semester.id=${semester.id}&schoolYear=1")}');
	}
[#else]
   bar.addItem("${b.text('action.print')}","print()");
   bar.addClose();
[/#if]
[/@]
<style>
.title {
    font-style: normal; 
    font-size: 10px;
}
.longText{
    font-size:10px;
}
.reportTable {
    border-collapse: collapse;
    border:solid;
    border-width:1px;
    border-color:#006CB2;
    vertical-align: middle;
    font-style: normal; 
    font-family:仿宋_GB2312;
    border-style: none;
[#if Parameters['notprint']??]
	font-size:14px;
[#else]
    font-size:10px;
[/#if]
    text-align:center;
    table-layout: fixed;
}
table.reportTable td{
    border:solid;
    border-width:0px;
    border-right-width:1;
    border-bottom-width:1;
    border-color:#006CB2;
    border-left-width: 1px;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-right-width: 1px;
}
</style>
 [@b.div id = "PrintA" width="100%" align="center" cellpadding="0" cellspacing="0"]
   [#assign tableIndex=0/]
    [#list multiStdGrades as multiStdGrade]
    [#if multiStdGrade.stdGrades?exists]
    [#list multiStdGrade.stdGrades?chunk(setting.pageSize) as stdGradeList]
    	<h3 style="margin-top:0px;margin-bottom:1px">
    	[#if semesters?? && semesters?size=1]
     	${multiStdGrade.semester.schoolYear}学年${multiStdGrade.semester.name}学期
     	[#else]
     	${multiStdGrade.semester.schoolYear}学年
     	[/#if]
     	[#if multiStdGrade.adminclass??]${multiStdGrade.adminclass.name}[/#if]${setting.gradeType.name}一览表
     	</h3>
	 [#assign tableIndex = tableIndex + 1/]
     <table width="100%" align="center" class="reportTable">
        [#assign TDCount = 4 + multiStdGrade.courses?size/]
        [#if setting.printGpa]
            [#assign TDCount = TDCount + 1/]
        [/#if]
        [#if (multiStdGrade.extraCourseNum > 0)]
            [#assign TDCount = TDCount + multiStdGrade.extraCourseNum*2/]
        [/#if]
	   <tr class="title">
	     <td width="3%">序号</td>
	     <td width="${100 / TDCount*2}%">${b.text("attr.stdNo")}</td>
	     <td width="${100 / TDCount}%">${b.text("attr.personName")}</td>
	     [#if setting.printGpa]<td width="${100 / TDCount}%">平均绩点</td>[/#if]
	     [#list multiStdGrade.courses as course]
	     <td width="${100 / TDCount}%">${course.name!}</td>
	     [/#list]
	     [#if (multiStdGrade.extraCourseNum>0)]
	     [#list 1..multiStdGrade.extraCourseNum as i]
	     <td width="${100 / TDCount*2}%" >课程、${b.text("attr.credit")}、成绩</td>
	     [/#list]
	     [/#if]
	   </tr>
	   [#list stdGradeList as stdGrade]
	    <tr align="center">
		   <td>${stdGrade_index+1}</td>
		   <td>${stdGrade.std.code}</td>
		   <td>${stdGrade.std.name}</td>
		   [#if setting.printGpa]<td>${(stdGrade.stdGpa.gpa)!}</td>[/#if]
		   [#assign gradeMap = stdGrade.toGradeMap()/]
		   [#list multiStdGrade.courses as course]
		   [#assign courseId=course.id?string]
		   [#assign examGrade=gradeMap[courseId]!'null']
		   [#if examGrade!='null']
		     <td [#if !examGrade.passed]style="background:pink;"[/#if]>
		         ${examGrade.scoreText!'--'}
		         [#if ((examGrade.courseGrade.courseTakeType.id)!1)!=1]<super>${examGrade.courseGrade.courseTakeType.name}</super>[/#if]
		         [#list  (gradeMap[course.id?string].examGrades)?if_exists  as examGrade][#if 4==examGrade.gradeType.id&&examGrade.published]<sup>*</sup>[#break][/#if][/#list]
	         </td>
	       [#else]<td></td>[/#if]
	       [/#list]
	       
		   [#if (multiStdGrade.extraCourseNum>0)]
		     [#assign emptyTdNum = multiStdGrade.extraCourseNum/]
		     [#if multiStdGrade.extraGradeMap[stdGrade.std.id?string]?exists]
		       [#list multiStdGrade.extraGradeMap[stdGrade.std.id?string] as courseGrade]
		         	<td [#if ((courseGrade.passed)?exists)&&!(courseGrade.passed)]style="background:pink;"[/#if]>
		         	<span class="longText">${courseGrade.course.name!},${courseGrade.course.credits},${courseGrade.getScoreText(setting.gradeType)!'--'}
			          [#if (courseGrade.courseTakeType.id)?default(-1)==5]免修[#else]
			            [#list  courseGrade.examGrades  as examGrade][#if 4==examGrade.gradeType.id &&examGrade.published ]<sup>*</sup>[#break][/#if][/#list]
			          [/#if]
			         </span>
		          </td>
		       [/#list]
		       [#assign emptyTdNum = multiStdGrade.extraCourseNum - multiStdGrade.extraGradeMap[stdGrade.std.id?string]?size/]
	         [/#if]
	         [#if emptyTdNum>0][#list 1..emptyTdNum as i]<td></td>[/#list][/#if]
	       [/#if]
	    </tr>
	   [/#list]
     </table>
  [/#list]
  [@b.div style="margin-top:10px;"][/@]
  [#else]
   [#if multiStdGrade.adminClass?if_exists.name?exists]
 	 	  	班级：${multiStdGrade.adminClass.name}，没有找到该学期的成绩。<br>
   [#else]
 	 	  没有找到该学期的成绩。<br>
   [/#if]
  [/#if]
  [/#list]
  [/@]
 </body>
[@b.foot/]
<SCRIPT LANGUAGE="javascript">
 function AllAreaExcel()  {
  var oXL= newActiveX("Excel.Application");
  if(null==oXL) return;
  var oWB = oXL.Workbooks.Add(); 
  var oSheet = oWB.ActiveSheet;  
  var sel=document.body.createTextRange();
  sel.moveToElementText(PrintA);
  sel.select();
  sel.execCommand("Copy");
  oSheet.Paste();
  oXL.Visible = true;
 }

 function AllAreaWord() {
  var oWD= newActiveX("Word.Application");
  if(null==oWD) return;
  var oDC = oWD.Documents.Add("",0,1);
  var oRange =oDC.Range(0,1);
  var sel = document.body.createTextRange();
  sel.moveToElementText(PrintA);
  sel.select();
  sel.execCommand("Copy");
  oRange.Paste();
  oWD.Application.Visible = true;
  //window.close();
 }
</SCRIPT>
