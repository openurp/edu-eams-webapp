[#ftl/]
[@b.head]
	<script type='text/javascript' src='${base}/dwr/interface/studentService.js'></script>
	<script src='${base}/dwr/interface/teachTaskDwrService.js'></script>
[/@]
[#include "/template/macros.ftl"/]
[@b.toolbar title="学生成绩添加(按课程序号－单个学生)"]
	bar.addBack();
	
	function selCourseByLessonNo(form){
		if($('#courseLessonNo').val() ==""){
			$('#c_courseName').html("<span style='color:red'>请填写正确的课程代码</span>");
			setVlaueIsNul();
			return false;
		}
		var res = jQuery.post("${b.url('std-grade!getCourseInfo')}",{lessonNo:$('#courseLessonNo').val(),semesterId:jQuery('input[name=courseGradeSemesterId]').val()},function(){
			if (res.readyState == 4 && res.status == 200 && res.responseText!=""){
				var course = jQuery.parseJSON(res.responseText);
				var message = "";
				if(course.responseStr == "noData"){
					message = "请输入正确的课程序号!";
				}else if(course.responseStr == "dataExeption"){
					message = "该课程序号对应的课程数据异常,请联系管理员!";
				}else if(course.responseStr == "reqIsNull"){
					message = "课程序号为空!";
				}else{
					$('#c_lessonId').val(course.lessonId);
					$('#c_courseNo').html(course.courseCode);
					$('#c_courseName').html(course.courseName);
					$('#markStyleValue').html(course.markStyleName);
					$('#markStyleId').val(course.markStyleId);
					$('#semesterId').val(course.semesterId);
				}
				if(message != ""){
					$('#c_courseName').html("<span style='color:red'>"+message+"</span>");
					setVlaueIsNul();
				}
			}
		});
	}

	function  setVlaueIsNul(){
		$('#courseLessonNo').val('');
		$('#c_lessonId').val('');
		$('#c_courseNo').html('');
		$('#markStyleValue').html('');
		$('#markStyleId').val('');
	}
	
	
	function getStdInfo(){
		if($('#c_lessonId').val()==""){
			$('#c_stdName').html("<span style='color:red'>请填写正确的课程序号!</span>");
			return false;
		}
		if($('#courseGradeStdCode').val()==""){
			$('#c_stdName').html("<span style='color:red'>学号为空!</span>");
			$('#c_stdId').val('');
			$('#courseGradeStdCode').val();
			return false;
		}
		var res = jQuery.post("${b.url('std-grade!getStuInfo')}",{lessonId:$('#c_lessonId').val(),stdCode:$('#courseGradeStdCode').val()},function(){
			if (res.readyState == 4 && res.status == 200 && res.responseText!=""){
				var message = "";
				var student = jQuery.parseJSON(res.responseText);
				if(student.responseStr == "noStu"){
					message="学号输入有误!";
				}else if (student.responseStr == "dataException"){
					message="该学号对应的学生数据异常,请联系管理员!";
				}else if (student.responseStr == "noTakes"){
					message="该学生没有选修该课程!";
				}else if (student.responseStr == "takeDataException"){
					message="该学生选修课程数据异常,请联系管理员!";
				}else if (student.responseStr == "reqIsNull"){
					message="课程或者学年学期为空!";
				}else{
					$('#c_stdId').val(student.stdId);
					$('#c_stdName').html(student.stdName);
				}
				if(message !=""){
					$('#c_stdName').html("<span style='color:red'>"+message+"</span>");
					$('#c_stdId').val('');	
				}
			}
		});
	}
	
	function saveAddGrade(form){
		if($('#c_lessonId').val()==""||$('#c_stdId').val() == ""){
				alert("当前数据信息可能正在获得中，或填写有误，\n请检查后试试保存。")	
				return false;
		}
		bg.form.submit(form,"${b.url('std-grade!saveAddGrade')}");
	}
	
	function checkScoreText(obj){
		var score = obj.value;
       if(""!=score){
         if(isNaN(score)){
            alert(score+" 不是数字");
            obj.value='';
         } else if(!/^\d*\.?\d*$/.test(score)) {
            alert("请输入0或正实数");
            obj.value='';
         } else if(parseInt(score)>100) {
            alert("百分制输入不允许超过100分");
           obj.value='';
         }
       }
	}
[/@]
<div style="background-color: #d1dCFF;border-width:2px 1px 0 1px;border-style: solid;border-color: #006CB2;padding:2px 10px;">
	[@eams.semesterCalendar label="学年学期" id="lessonSemesterId" name="courseGradeSemesterId" value=semester empty="false"/]
</div>

[@b.form name="actionAddGradeForm"]
	<table class="gridtable" width="80%">
		<tr>
		    <td class="griddata-odd" style="width:10%;text-align:right;" id="f_markStyleId">${b.text("attr.taskNo")}<font color="red">*</font>:</td>
		    <td class="" width="20%">
		    	[@b.textfield label="" id="courseLessonNo" name="courseGrade.lessonNo" value=""  style="width:100px" maxLength="32" onchange="selCourseByLessonNo(document.stdGradeForm)" /] 
		    </td>
			<td class="griddata-odd" style="width:10%;text-align:right;">${b.text("attr.courseNo")}:</td>
			<td width="30%" id="c_courseNo"></td>
			<td class="griddata-odd" style="width:10%;text-align:right;">${b.text("attr.courseName")}:</td>
			<td id="c_courseName"></td>
	     </tr>
		 <tr>
			<td class="griddata-odd" style="width:10%;text-align:right;">${b.text("std.code")}<font color="red">*</font>:</td>
			<td>[@b.textfield label="" id="courseGradeStdCode" name="courseGrade.std.code" value="" style="width:100px" maxLength="32" onChange="getStdInfo()"/]</td>
            <td class="griddata-odd" style="width:10%;text-align:right;">${b.text("attr.studentName")}:</td>
            <td id="c_stdName"></td>
          	<td class="griddata-odd" style="width:10%;text-align:right;">${b.text("entity.markStyle")}:</td>
            <td id="markStyleValue"></td>
		 </tr>
		<input type="hidden" name="courseGrade.course.id" id="courseGradeCourseId" value="">
	</table>
    [@b.div style="margin-top:10px;"/]
    [@b.grid items=gradeTypes var="gradeType"]
    	[@b.row]
	    	[@b.col title="考试成绩" id="f_examGrade${(gradeType.id)!}" width="30%" style="text-align:right;"][@i18nName gradeType/]:[/@]
	    	[@b.col title="得分和考试情况" style="text-align:left;"]
	            <input type="text" name="examGrade${(gradeType.id)!}.score" value="" onchange="checkScoreText(this)" maxLength="5" style="width:120px"/>
	            [@htm.i18nSelect datas=examStatuses name="examGrade" + gradeType.id + ".examStatus.id" selected="1" style="width:100px"/]
	    	[/@]
    	[/@]
    [/@]
	<input type="hidden" id="c_lessonId" name="courseGrade.lesson.id" value=""/>
    <input type="hidden" id="c_stdId" name="courseGrade.std.id" value=""/>
    <input type="hidden" id="markStyleId" name="courseGrade.markStyle.id" value=""/>	
    <input type="hidden" id="semesterId" name="courseGrade.semester.id" value="${addGradeSemesterId!}" />
    <table align="center">
        <tr>
            <td>
                <input type="button" onClick="saveAddGrade(document.actionAddGradeForm)" value="保存">
               	<input type="reset" value="重置">
            </td>
        </tr>
    </table>
[/@]
[@b.foot/]
