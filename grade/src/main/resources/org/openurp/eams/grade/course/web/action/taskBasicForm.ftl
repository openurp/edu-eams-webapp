[#ftl]
[@b.head/]
	[@b.textfields  names="lesson.no;attr.taskNo,lesson.course.code;attr.courseNo,lesson.course.name;attr.courseName"/]
	[@b.select  label="entity.courseType" name="lesson.courseType.id" items=courseTypes?sort_by(["name"]) empty="${b.text('common.all')}"/]
	[@b.select  label="attr.teach4Depart" name="lesson.teachClass.depart.id" items=(departmentList)?sort_by("code") empty="${b.text('common.all')}"/]
	[@b.select  label="attr.teachDepart" name="lesson.teachDepart.id" items=(teachDepartList)?sort_by("code") empty="${b.text('common.all')}"/]
	[@b.select  label="学生类别" name="stdTypeId" items=(stdTypeList)?sort_by("code") empty="${b.text('common.all')}"/]
	[@b.textfields  names="teacher.code;教师工号,teacher.name;教师姓名,fake_grade;std.grade,lesson.course.credits;attr.credit,lesson.courseSchedule.weekUnits;course.weekHour,lesson.courseSchedule.startWeek;course.weekFrom" /]
	${extraSearchTR?if_exists}
<script language="JavaScript">
    function validateInput(form){
       var errors="";
	        if(""!=form['lesson.course.credits'].value&&!/^\d*\.?\d*$/.test(form['lesson.course.credits'].value)){
	           errors+="学分"+form['lesson.course.credits'].value+"格式不正确，应为正实数\n";
	        }
	        if(""!=form['lesson.courseSchedule.weekUnits'].value&&!/^\d+$/.test(form['lesson.courseSchedule.weekUnits'].value)){
	           errors+="周课时"+form['lesson.courseSchedule.weekUnits'].value+"格式不正确，应为正整数\n";
	        }
	        if(""!=form['lesson.courseSchedule.startWeek'].value&&!/^\d+$/.test(form['lesson.courseSchedule.startWeek'].value)){
	           errors+="起始周"+form['lesson.courseSchedule.startWeek'].value+"格式不正确，应为正整数\n";
	        }
	        if(""!=errors){
	           alert(errors);return false;
	        }
        return true;
     }
</script>
[@b.foot/]