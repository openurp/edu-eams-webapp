[#ftl]
[@b.head/]
[@b.form name="actionForm" action="!search" target="contentDiv"]
[@b.grid items=courseGrades var="courseGrade" filterable="true"]
	[@b.gridbar]
		bar.addItem("打印成绩单","stdReport(document.actionForm)");
    	bar.addItem("查看详情","gradeInfo(document.actionForm)");
    	
		function gradeInfo(form){
	        bg.form.submitId(form,"courseGrade.id",false,"${b.url('std-grade-search!info')}");
	        form.action="${b.url('std-grade-search!search')}";
	 	}
	 	
	    function stdReport(form){
	    	form.target="_blank";
	      	bg.form.submitId(form,"courseGrade.id",false,"${b.url('std-grade-search!stdReport')}");
	      	form.action="${b.url('std-grade-search!search')}";
	      	form.target="contentDiv";
	    }
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="std.code" title="attr.stdNo" width="10%"/]
		[@b.col property="std.name" title="attr.personName" width="10%"]
			[@b.a href="!info?courseGrade.id=${(courseGrade.id)?default('false')}" target="contentDiv" title="查看成绩详情"]${(courseGrade.std.name)?if_exists}[/@]
		[/@]
		[@b.col property="lessonNo" title="attr.taskNo" width="10%"/]
		[@b.col property="course.code" title="attr.courseNo" width="15%"/]
		[@b.col property="course.name" title="entity.course" width="20%"/]
		[@b.col property="courseType.name" title="entity.courseType" width="20%"/]
		[@b.col property="score" title="成绩" width="5%"][#if courseGrade.passed]${(courseGrade.scoreText)?if_exists}[#else]<font color="red">${(courseGrade.scoreText)?if_exists}</font>[/#if][/@]
		[@b.col property="course.credits" title="attr.credit" width="5%"/]
		[@b.col property="gp" title="绩点" width="5%"][#if courseGrade.passed]${(courseGrade.gp?string("#.##"))?if_exists}[#else]<font color="red">${(courseGrade.gp?string("#.##"))?if_exists}</font>[/#if][/@]
		[#--[@b.col property="semester" title="学年学期" width="15%"]${(courseGrade.semester.schoolYear)?if_exists}学年 ${(courseGrade.semester.name)?if_exists?replace('0','第')}学期[/@]--]
	[/@]
[/@]
[/@]
[@b.foot/]
