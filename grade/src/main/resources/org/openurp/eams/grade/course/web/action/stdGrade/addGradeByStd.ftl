[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="学生成绩添加(按课程序号－单个学生)"]
	bar.addBack("${b.text("action.back")}");
[/@]
	[@b.form name="actionForm" action="" theme="list" title="基本信息"]
		<table class="infoTable" width="100%">
			[#include "semesterWithProjectAddGrade.ftl"/]
		    <tr>
	            <td class="title" id="f_course">${b.text('attr.taskNo')}:</td>
	            <td><input type="text" id="courseGrade.taskSeqNo" name="courseGrade.taskSeqNo" value="" maxlength="5" style="width:115px" onblur="getTeachTaskInfo(event)"/></td>
	            <td class="title">${b.text("attr.courseNo")}:</td>
	            <td id="c_courseNo"></td>
	            <td class="title">${b.text("attr.courseName")}:</td>
	            <td id="c_courseName"></td>
		    </tr>
            <tr>
		        <td class="title" id="f_student">${b.text("std.code")}:</td>
		        <td>${std.code}</td>
		        <td class="title">${b.text("attr.studentName")}:</td>
		        <td id="c_stdName">${std.name}</td>
		        <td class="title">${b.text("entity.markStyle")}:</td>
		        <td id="markStyleValue"></td>
        	</tr>
	    </table>
	    <input type="hidden" name="courseGrade.id" value=""/>
	    <input type="hidden" id="markStyleId" name="courseGrade.markStyle.id" value=""/>
	    <input type="hidden" id="c_stdId" name="courseGrade.std.id" value="${std.id}"/>
	    <input type="hidden" id="c_lessonId" name="courseGrade.task.id" value=""/>
	    [@b.div style="margin-top:10px;"/]
		[@b.grid items=gradeTypes var="gradeType"]
			[@b.row]
				[@b.col title="考试成绩" id="f_examGrade${gradeType.id}" width="30%"][@i18nName gradeType/][/@]
				[@b.col title="得分和考试情况" width="70%"]
	            	<input type="text" name="examGrade${gradeType.id}.score" value="" maxlength="5" style="width:120px"/>
	            	[#assign name]examGrade${gradeType.id}.examStatus.id[/#assign]
	            	[@htm.i18nSelect datas=examStatuses name=name selected="1" style="width:100px"/]
				[/@]
			[/@]
		[/@]
	[/@]
	<table align="center">
		<tr>
		    <td>
                <input type="button" onclick="saveAndAddNext()" value="保存并添加下一门成绩">
		        <input type="button" onclick="saveAddGrade()" value="保存">
		       	<input type="button" onclick="formReset()" value="重置">
		    </td>
		</tr>
	</table>
[@b.foot/]
