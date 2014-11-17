[#ftl]
[@b.head/]
[@b.toolbar title= ("<font color='blue'>"+adminclass.name+"</font>学生列表")/]
[@eams.semesterBar action="!search" name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester]
	<input type="hidden" name="adminclass.id" value="${adminclass.id}"/>
[/@]
[@b.form name="gradeForTeacherInfoForm" target="main" action="!info"]
	<input type="hidden" name="semester.id" value="${semester.id}"/>
[/@]
	[@b.grid items=adminclass.students?sort_by("code") var="student"]
		[@b.gridbar]
			bar.addItem("查询成绩","info()");
			
			function info(){
				var studentIds = bg.input.getCheckBoxValues("student.id");
				if(studentIds==null || studentIds==""||studentIds.indexOf(",")>-1){
					alert("请仅选择一条操作");
					return;
				}
				var form = document.gradeForTeacherInfoForm;
				bg.form.addInput(form,"student.id",studentIds); 
				bg.form.submit(form);
			}
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="code" title="attr.stdNo"/]
			[@b.col property="name" title="attr.name"/]
			[@b.col property="gender.name" title="entity.gender"/]
			[@b.col property="grade" title="adminClass.grade"/]
			[@b.col property="department.name" title="common.college"/]
			[@b.col property="major.name" title="entity.major"/]
		[/@]
	[/@]
[@b.foot/]
