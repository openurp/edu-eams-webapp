[#ftl]
[@b.head/]
[@b.grid items=makeupCourses var="makeupCourse" sortable="false"]
	[@b.gridbar title="${b.text('filed.delayApplyList')}"]
		bar.addItem("${b.text('action.print')}","printGrade()");
		bar.addItem("${b.text('filed.makeupRestudy.exam.input')}", 'batchAddGrade()');
		bar.addItem('${b.text("grade.fillHuankaoResultsTengVolumes")}','printEmptyGradeTable()');
  	
		function printEmptyGradeTable(){
			var form =  document.makeupGradeListForm;
			form.target="_blank";
			bg.form.submitId(form,"makeupCourse.id",true,"${b.url('makeup!gradeTable')}");
		}
		
		function batchAddGrade(){
			var form =  document.makeupGradeListForm;
			form.target="";
			bg.form.addParamsInput(form,"${b.paramstring}");
			bg.form.submitId(form,"makeupCourse.id",false,"${b.url('makeup!batchAddGrade')}");
		}
		
		function printGrade(){
			var form =  document.makeupGradeListForm;
			form.target="_blank";
			bg.form.submitId(form,"makeupCourse.id",false,"${b.url('makeup!gradeInfo')}");
		}
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="depart.name" title="attr.teachDepart"/]
		[@b.col property="course.code" title="attr.courseNo"/]
		[@b.col property="course.name" title="attr.courseName"/]
		[@b.col property="count" title="${b.text('attr.personCount')}" /]
	[/@]
[/@]
[@b.form name="makeupGradeListForm"]
	<input type="hidden" name="semester.id" value="${Parameters['examTake.semester.id']?default('')}"/>
[/@]
[@b.foot/] 
