[#ftl]
[@b.head/]
[@b.toolbar title="不及格成绩学生列表"]
	bar.addClose();
[/@]
[@b.form name="noGradeTakeListForm" action="!unPassedGrades"]
	<input type="hidden" name="lesson.semester.id" value="${semesterId!}">
	[@b.grid items=unPassedGrades?if_exists var="grade" filterable="true"]
		[@b.gridbar]
			bar.addItem("${b.text('action.export')}","exportData(document.noGradeTakeListForm)");
	    	function exportData(form){
	    		var gradeIds = bg.input.getCheckBoxValues("grade.id");
				if (gradeIds == "" && !confirm("是否导出所有查询结果？")) return;
	        	bg.form.addInput(form,"gradeIds", gradeIds);
	        	
		        bg.form.addInput(form,"keys","std.code,std.name,taskSeqNo,course.code,course.name,score");
		        bg.form.addInput(form,"titles","学号,姓名,课程序号,课程代码,课程名称,成绩");
		        bg.form.addInput(form, "kind", "unPassedGrades", "hidden");
		        bg.form.submit(form,"${b.url('!export')}");
		        form.target="contentDiv";
				form.action="${b.url('!search')}";
	   	    }
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="std.code" title="attr.stdNo"][@b.a href="/studentSearch!info?studentId=${grade.std.id}" target="_blank"]${grade.std.code}[/@][/@]
			[@b.col property="std.name" title="attr.personName"/]
			[@b.col property="lesson.no" title="attr.taskNo"/]
			[@b.col property="course.code" title="attr.courseNo"/]
			[@b.col property="course.name" title="attr.courseName"/]
			[@b.col property="score" title="成绩"]<font color="red">${grade.getScore()}</font>[/@]
		[/@]
	[/@]
[/@]
[@b.foot/]