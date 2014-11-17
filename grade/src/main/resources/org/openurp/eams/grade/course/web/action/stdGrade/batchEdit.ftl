[#ftl/]
[#include "/template/macros.ftl"/]
[@b.head/]
[@b.toolbar title="批量修改成绩(${grades?size})"]
	bar.addItem("提交修改","save(document.gradeForm)");
	bar.addBack("${b.text("action.back")}");
	   var form= document.gradeForm; 
	   function save(form) {
		 if (!form['course.code'].disabled && form['course.code'].value == "") {
		 	alert("请填写课程代码.");
		 	return false;
		 }
		 if(form['course.code'].disabled&&form['courseType.id'].disabled){
		 	alert("请填写修改项！");
		 	return false;
		 }
		 if (confirm("确定要对这些成绩进行选定的修改吗?")) {
			bg.form.submit(form);
		 }
	   }
	   
	    var modifyed={};
		var infos=new Object();
		infos['course.code']='请输入课程代码';
		function changeState(check, name) {
		  form[name].disabled =! check.checked;
		  if (form[name].type == "text") {
			form[name].value = infos[name];
		  }
		}
[/@]
[@b.form name="gradeForm" action="!saveBatchEdit" target="contentDiv"]
	<input type="hidden" name="courseGradeIds" value="${courseGradeIds}">
	<table width="100%" align="center" class="formTable">
	   <tr>
	  	 <td class="title" width="20%"><input type="checkbox" onclick="changeState(this,'course.code')">&nbsp;课程代码:</td>
	     <td><input name="course.code" maxlength="32" value="请输入课程代码" onfocus="this.value=''" disabled/></td>
	  	 <td class="title" width="20%"><input type="checkbox" onclick="changeState(this,'courseType.id')">&nbsp;课程类别:</td>
	     <td>
	     <select name="courseType.id" style="width:150px" disabled="true">
	     	[#list courseTypeList?sort_by("name") as courseType]
	     		<option value="${courseType.id}">${courseType.name}</option>
	     	[/#list]
	     </select>
	   </tr>
	</table>
[/@]
[@b.grid items=grades var="grade"]
	[@b.row]
		[@b.col title="序号"]${grade_index+1}[/@]
		[@b.col title="attr.stdNo"]${grade.std.code}[/@]
		[@b.col title="attr.personName"][@i18nName grade.std/][/@]
		[@b.col title="attr.courseNo"]${grade.course.code}[/@]
		[@b.col title="entity.course"][@i18nName grade.course?if_exists/][/@]
		[@b.col title="entity.courseType"][@i18nName grade.courseType?if_exists/][/@]
		[@b.col title="成绩"][#if grade.passed]${(grade.getScore())?if_exists}[#else]<font color="red">${(grade.getScore())?if_exists}</font>[/#if][/@]
		[@b.col title="attr.credit"]${grade.course.credits}[/@]
		[@b.col title="绩点"]${(grade.gp?string("#.##"))?if_exists}[/@]
		[@b.col title="学年学期"]${(grade.semester.schoolYear)?if_exists}学年 ${(grade.semester.name)?if_exists?replace('0','第')}学期[/@]
	[/@]
[/@]
[@b.foot/]
[#--]
	<#include "/template/head.ftl"/>
	<BODY LEFTMARGIN="0" TOPMARGIN="0" > 
	<table id="myBar"></table>
	<table width="100%" align="center" class="formTable">
	  <form name="gradeForm" action="stdGrade.action?method=saveBatchEdit" method="post" >
	   <input type="hidden" name="params" value="${Parameters['params']}">
	   <input type="hidden" name="courseGradeIds" value="${Parameters['courseGradeIds']}">
	   <tr>
	  	 <td class="title" width="20%"><input type="checkbox" onclick="changeState(this,'course.code')">&nbsp;课程代码:</td>
	     <td><input name="course.code" maxlength="32" value="请输入课程代码" onfocus="this.value=''" disabled/></td>
	  	 <td class="title" width="20%"><input type="checkbox" onclick="changeState(this,'courseType.id')">&nbsp;课程类别:</td>
	     <td><@htm.i18nSelect datas=courseTypeList?sort_by("name") selected="" name="courseType.id" style="width:150px" disabled="true"/></td>
	   </tr>
	</form> 
	</table>
	<@table.table width="100%" id="listTable">
		<@table.thead>
		<@table.td class="selectTd" text="序号"/>
		<@table.td width="10%" name="attr.stdNo"/>
		<@table.td width="10%"  name="attr.personName"/>
		<@table.td width="8%"  name="attr.courseNo"/>
		<@table.td width="20%" name="entity.course"/>
		<@table.td width="15%" name="entity.courseType"/>
		<@table.td width="5%" text="成绩"/>
		<@table.td width="5%" name="attr.credit"/>
		<@table.td width="5%" text="绩点"/>
		<@table.td width="15%" text="学年学期"/>
	</@>
	<@table.tbody datas=grades;grade,grade_index>
	    <td>${grade_index+1}</td>
		<td>${grade.std.code}</td>
		<td><@i18nName grade.std/></td>
		<td>${grade.course.code}</td>
		<td><@i18nName grade.course?if_exists/></td>
		<td><@i18nName grade.courseType?if_exists/></td>
		<td<#if !grade.passed> style="color:red"</#if>>${grade.getScoreText()?if_exists}</td>
		<td>${grade.course.credits}</td>
		<td<#if !grade.passed> style="color:red"</#if>>${(grade.gp?string("#.##"))?if_exists}</td>
		<td>${grade.semester.schoolYear} ${grade.semester.name}</td>
	   </@>
	</@>
	</body>
	<script>
	   var bar = new ToolBar("myBar","批量修改成绩（${grades?size}）",null,true,true);
	   bar.addItem("提交修改","save()");
	   bar.addBackOrClose("<@text name="action.back"/>", "<@text name="action.close"/>");
	   var form= document.gradeForm; 
	   function save() {
		 if (!form['course.code'].disabled && form['course.code'].value == "") {
		 	alert("请填写课程代码.");
		 	return;
		 }
		 if(form['course.code'].disabled&&form['courseType.id'].disabled){
		 	alert("请填写修改项！");
		 	return;
		 }
		 if (confirm("确定要对这些成绩进行选定的修改吗?")) {
			form.submit();
		 }
	   }
	   
	    var modifyed={};
		var infos=new Object();
		infos['course.code']='请输入课程代码';
		function changeState(check, name) {
		  form[name].disabled =! check.checked;
		  if (form[name].type == "text") {
			form[name].value = infos[name];
		  }
		}
	</script>
	<#include "/template/foot.ftl"/>
[--]
