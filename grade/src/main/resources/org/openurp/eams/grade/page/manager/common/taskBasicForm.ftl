[#ftl]
[@b.head/]
	[#if inputSwitches?? && inputSwitches?size > 0]
		[@b.select label="录入批次" name="fake.inputSwitch.id" items=inputSwitches empty="..."/]
	[/#if]
	[@b.textfields  names="lesson.no;attr.taskNo,lesson.course.code;attr.courseNo,lesson.course.name;attr.courseName"/]
	[@b.textfields  names="limitGroup.grade;std.grade" /]
	[@b.select  label="entity.courseType" name="lesson.courseType.id" items=courseTypes?sort_by(["name"]) empty="..."/]
	[@b.select  label="attr.teachDepart" name="lesson.teachDepart.id" items=(teachDepartList)?sort_by("code") empty="..."/]
	[@b.textfields  names="teacher.name;教师姓名" /]
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