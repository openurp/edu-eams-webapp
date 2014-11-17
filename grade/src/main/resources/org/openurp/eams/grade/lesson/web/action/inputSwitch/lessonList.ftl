[#ftl]
[@b.head/]
[@b.toolbar title="设置可录入成绩的教学任务"]
	bar.addItem("后退", action.method("index"), "backward.png");
[/@]

[#assign type = Parameters["type"]!("UNUSED") /]

[@b.form name="inputableLessonListForm" action="!lessonList" target="input-switches"]	
	<input type="hidden" name="gradeInputSwitch.id" value="${Parameters["gradeInputSwitch.id"]}"/>
	<select name="type" onChange="bg.form.submit(this.form)" style="width:250px">
		<option value="UNUSED" [#if type=='UNUSED']selected[/#if]>本次不开放录入的任务</option>
		<option value="USED"   [#if type=='USED']  selected[/#if]>本次开放录入的任务</option>
	</select>
	[@b.grid id="inputableLessonGrid" items=lessons var="lesson" filterable="true"  target="input-switches" title="" ]
		[@b.gridbar title=""]		
			[#if type=='UNUSED']
				bar.addItem("添加", action.multi("addLessons"));
				bar.addItem("添加全部任务", action.method("addAllLessons"));
				[#if examBatches?size > 0]
				var menu = bar.addMenu("添加排考批次的任务");
				[#list examBatches as batch]
				menu.addItem("${batch.name?js_string}", action.method("pullFromExamBatch", null, "examBatch.id=${batch.id}"));
				[/#list]
				[/#if]
			[#else]
				bar.addItem("删除", action.multi("removeLessons","确认删除?"));
				bar.addItem("删除全部任务", action.method("removeAllLessons", "确认删除全部任务?"));
			[/#if]
		[/@]
		[@b.gridfilter property="courseType.name"]
			<select name="lesson.courseType.id" style="width:95%" onchange="bg.form.submit(this.form)">
				<option value="">...</option>
				[#list courseTypes?sort_by('code') as courseType]
				<option value="${courseType.id}" [#if (Parameters['lesson.courseType.id']!"")="${courseType.id}"]selected="selected"[/#if]>${courseType.name}</option>
				[/#list]
			</select>
		[/@]
		[@b.row]
			[@b.boxcol /]
			[@b.col width="8%" property="no" title="序号" /]
			[@b.col width="8%" property="course.code" title="attr.courseNo" maxlength="3"/]
			[@b.col width="12%" property="course.name" title="attr.courseName" /]
			[@b.col width="9%" property="courseType.name" title="entity.courseType" /]
			[@b.col width="13%" property="teachClass.name" title="entity.teachClass"]${(lesson.teachClass.name?html)!}[/@]
			[@b.col width="5%" property="teachClass.stdCount" title="人数" /]
			[@b.col width="5%" property="course.credits" title="attr.credit" /]
			[@b.col width="5%" property="course.weekHour" title="teachTask.weeksPerHour" /]
			[@b.col width="5%" property="courseSchedule.startWeek" title="起始周" /]
			[@b.col width="5%" property="courseSchedule.endWeek" title="结束周" /]
			[@b.col width="5%" property="coursePeriod" title="course.period"/]
			[@b.col width="8%" property="teachDepart.name" title="开课院系"/]
			[@b.col width="7%" property="teacherName" title="entity.teacher" sortable="false"][#list lesson.teachers! as teacher]${teacher.name!}[#if teacher_has_next],[/#if][/#list][/@]
		[/@]
	[/@]
[/@]