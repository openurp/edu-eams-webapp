[#ftl]
[@b.head/]
[@b.toolbar title="无成绩学生列表"]
	bar.addClose();
[/@]
[@b.form name="noGradeTakeListForm" action="!noGradeTakes"]
	<input type="hidden" name="lesson.semester.id" value="${semeterId}">
	[@b.grid items=noGradeTakes?if_exists var="take" filterable="true"]
		[@b.gridbar]
			bar.addItem("${b.text('action.export')}","exportData(document.noGradeTakeListForm)");
	        function exportData(form){
	        	var takeIds = bg.input.getCheckBoxValues("take.id");
				if (takeIds == "" && !confirm("是否导出所有查询结果？")) return;
	        	bg.form.addInput(form,"takeIds", takeIds);
	        	
			    bg.form.addInput(form,"keys","lesson.semester.schoolYear,lesson.semester.name,std.code,std.name,lesson.no,lesson.course.code,lesson.course.name,lesson.teacherNames,lesson.course.credits,lesson.courseType.name,courseTakeType.name,std.type.name");
			    bg.form.addInput(form,"titles","学年度,学期,${b.text('attr.stdNo')},${b.text('attr.personName')},${b.text('attr.taskNo')},${b.text('attr.courseNo')},${b.text('attr.courseName')},授课教师,课程学分,${b.text('entity.courseType')},修读类别,${b.text('entity.studentType')}");
			    bg.form.addInput(form, "kind", "noGradeTakes", "hidden");
			    bg.form.submit(form,"${b.url('!export')}","_self",null,false);
			    form.target="contentDiv";
				form.action="${b.url('!noGradeTakes')}";
	   	   }
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="std.code" title="attr.stdNo"][@b.a href="/studentSearch!info?studentId=${take.std.id}" target="_blank"]${take.std.code}[/@][/@]
			[@b.col property="std.name" title="attr.personName"/]
			[@b.col property="std.grade" title="std.grade"/]
			[@b.col property="std.department.name" title="管理院系"/]
			[@b.col property="lesson.no" title="attr.taskNo"/]
			[@b.col property="lesson.course.code" title="attr.courseNo"/]
			[@b.col property="lesson.course.name" title="attr.courseName"/]
		[/@]
	[/@]
[/@]
[@b.foot/]
