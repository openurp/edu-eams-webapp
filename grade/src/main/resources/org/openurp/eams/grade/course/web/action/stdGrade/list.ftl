[#ftl]
[@b.head/]
[@b.messages/]
[@b.form name="stdGradeListForm" action="!search" target="contentDiv"]
[@b.grid items=courseGrades var="courseGrade" filterable="true"]
	[@b.gridbar]
		bar.addItem("${b.text('action.info')}","gradeInfo()");
	    var menuBar = bar.addMenu("${b.text('action.add')}(按代码)","batchAddGrade()");
	    	menuBar.addItem("${b.text('action.add')}(按序号)","addGrade()");
	    bar.addItem("${b.text('action.edit')}","editGrade()");
	    bar.addItem("${b.text('action.export')}","exportData()");
	    bar.addItem("${b.text('action.delete')}","removeGrade()");
	    bar.addItem("批量修改","batchEdit()");
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="std.code" title="attr.stdNo" width="10%"]
			[@b.a href="/studentSearch!info?studentId=${courseGrade.std.id}" target="contentDiv" title="查看学生基本信息"]${(courseGrade.std.code)?if_exists}[/@]
		[/@]
		[@b.col property="std.name" title="attr.personName" width="10%"]
			[@b.a href="!info?courseGrade.id=${(courseGrade.id)?default('false')}" target="contentDiv" title="查看成绩详情"]${(courseGrade.std.name)?if_exists}[/@]
		[/@]
		[@b.col property="lessonNo" title="attr.taskNo" width="7%"/]
		[@b.col property="course.code" title="attr.courseNo" width="9%"/]
		[@b.col property="course.name" title="entity.course" width="20%"/]
		[@b.col property="courseType.name" title="entity.courseType" width="15%"/]
		[@b.col property="score" title="成绩" width="5%"]
			[#if courseGrade.passed]${(courseGrade.scoreText)?if_exists}[#else]<font color="red">${(courseGrade.scoreText)?if_exists}</font>[/#if]
		[/@]
		[@b.col property="course.credits" title="attr.credit" width="3%"/]
		[@b.col property="gp" title="绩点" width="3%"][#if courseGrade.passed]${(courseGrade.gp?string("#.##"))?if_exists}[#else]<font color="red">${(courseGrade.gp?string("#.##"))?if_exists}</font>[/#if][/@]
		[@b.col title="学年学期" width="18%"]${(courseGrade.semester.schoolYear)?if_exists}学年第${(courseGrade.semester.name)?if_exists?replace('0','第')}学期[/@]
	[/@]
[/@]
[/@]

[@b.form name="actionForm" action="" target="_blank"/]
  	<script language="JavaScript">
		
	    function editGrade(){
	    	var form=document.stdGradeListForm;
			bg.form.submitId(form,"courseGrade.id",false,"${b.url('std-grade!edit')}");
			form.action="${b.url('std-grade!search')}";
	    }
	    
	    function removeGrade(){
	    	var form=document.stdGradeListForm;
			bg.form.addInput(form,"params","[@htm.queryStr/]");
			
			var gradeIds = bg.input.getCheckBoxValues("courseGrade.id");
    	if (gradeIds == null || gradeIds == "") {
    		alert("请选择一个或多个成绩进行操作!");
    	}
    	   	
			if(confirm("确定删除成绩?")){
				bg.form.submitId(form,"courseGrade.id",true,"${b.url('std-grade!removeGrade')}");
				form.action="${b.url('std-grade!search')}";
			}
	    }
	    
	    function batchEdit(){
	    	var form=document.stdGradeListForm;
	    	bg.form.submitId(form,"courseGrade.id",true,"${b.url('std-grade!batchEdit')}");
	    	form.action="${b.url('std-grade!search')}";
	    	
	    }
	    
	    function gradeInfo(){
	    	var form=document.stdGradeListForm;
	        bg.form.submitId(form,"courseGrade.id",false,"${b.url('std-grade!info')}");
	        form.action="${b.url('std-grade!search')}";
	    }
	    
	    function exportData(){
	    	var form=document.stdGradeForm;
	       	form.target="_self";
	       	if(confirm("是否导出已经查询出的所有成绩？")){
	          	bg.form.addInput(form,"keys","semester.schoolYear,semester.name,std.code,std.name,std.adminclass.name,lessonNo,course.code,course.name,lesson.getTeacherNames(),course.credits,score,creditAcquired,gp,courseType.code,courseType.name,courseTakeType.name,std.type.name");
         	 	bg.form.addInput(form,"titles","学年度,学期,${b.text('attr.stdNo')},${b.text('attr.personName')},班级,${b.text('attr.taskNo')},${b.text('attr.courseNo')},${b.text('attr.courseName')},授课教师,课程学分,分数,实得学分,绩点,课程类别代码,${b.text('entity.courseType')},修读类别,${b.text('entity.studentType')}");
	          	bg.form.addInput(form,"fileName","成绩管理-学生成绩导出");
	          	bg.form.submit(form,"${b.url('std-grade!export')}");
	       }
      		form.target="contentDiv";
          	form.action="${b.url('std-grade!search')}";
	    }
	    
	    function batchAddGrade(){
			var form=document.stdGradeForm;
	    	bg.form.submit(form,"${b.url('std-grade!batchAdd')}");
	    	form.action="${b.url('std-grade!search')}";
	    }
	    
	    function addGrade() {
	    	var form=document.stdGradeForm;
	    	bg.form.submit(form,"${b.url('std-grade!addGrade')}");
	    	form.action="${b.url('std-grade!search')}";
	    }
  </script>
[@b.foot/]
