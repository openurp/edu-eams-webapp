[#ftl]
[#--成绩状态的修改权限--]
[#assign authority=0/]
[@ems.guard res="/teach/grade/lesson/audit"][#assign authority=1/][/@]
[@ems.guard res="/teach/grade/lesson/manage"][#assign authority=2/][/@]

[#assign gradeStatus={'0':'新添加', '1':'已提交', '2':'已发布'}/]
[@b.toolbar title="成绩状态"]
	[#if gradeState.status <= 2]
   		bar.addItem("${b.text('action.save')}", "save()");
   		
	    var form =document.collegeGradeEditForm;
	    function save(){
	    	var percent = 0;
			var flag = false;
			jQuery("input[name*='percent']").each(function(){
				if(jQuery("input[name='"+jQuery(this).prop("name").replace("percent","")+"id']").is(":checked")){
					flag = true;
				}
				if(!/^\d+$/.test(jQuery(this).val())){
				}else{
				 	percent = percent + parseFloat(jQuery(this).val());
				}
			});
			if(percent != 100 && flag){
		        alert("所有设置的百分比数值之和必须是100％。");
        		return;
			}
			bg.form.addInput(form,"status","${status!}");
	        if (confirm("确认提交现有成绩吗？")) {
	           	bg.form.submit(form,form.action);
	        }
		}
   	[/#if]
	bar.addBack();
[/@]
[@b.form name="collegeGradeEditForm"  target="contentDiv" action="!saveGradeState"]

    <table width="100%" class="formTable">
        <tr>
            <td class="title" width="12%">${b.text('attr.taskNo')}:</td>
            <td class="content" width="22%">${(lesson.no)!}</td>
            <td class="title" width="12%">${b.text('attr.courseNo')}:</td>
            <td class="content" width="22%">${(lesson.course.code)!}</td>
            <td class="title" width="12%">${b.text('attr.courseName')}:</td>
			<td class="content">${(lesson.course.name)!}[#if lesson.subCourse??]<sup style="color:#29429E">${lesson.subCourse.name!}</sup>[/#if]</td>
        </tr>
        <tr>
            <td class="title">授课教师:</td>
            <td class="content">
            	[#list lesson.teachers?if_exists as teacher]
            		[#if teacher_index!=0],[/#if]${teacher.name!}
            	[/#list]
            </td>
            <td class="title">考核方式:</td>
            <td class="content">${(lesson.course.examMode.name)!}</td>
            <td class="title">上次录入时间:</td>
			<td class="content">[#if gradeState.inputedAt??]${gradeState.inputedAt?string("yyyy-MM-dd HH:mm")}[/#if]</td>
        </tr>
        <tr>
            <td class="title">记录方式:</td>
            <td class="content">${(gradeState.scoreMarkStyle.name)!}</td>
            <td class="title">成绩录入精确度:</td>
            <td class="content">[#if gradeState.precision==0]保留整数[#else]保留一位小数[/#if]</td>
            <td class="title">状态:</td>
			<td class="content">${gradeStatus[gradeState.status?string]}</td>
        </tr>
	</table>
	
	<table  class="gridtable" width="100%" id="gradePercent" >
        <tr class="gridhead">
        	<td width="5%"></td>
        	<td width="15%">成绩类型</td>
        	<td width="20%">记录方式</td>
        	<td width="10%">百分比</td>
        	<td width="20%">成绩录入精确度</td>
        	<td width="10%">状态</td>
        	<td width="20%">上次录入</td>
        </tr>
        [#list gradeState.states?sort_by(["gradeType","code"]) as state]
        <tr align="center">
        	<td><input type="checkbox" name="state${state.gradeType.id}.id" value="${state.id!}"[#if (state.id)??] checked[/#if]/></td>
            <td>${state.gradeType.name}</td>
            <td>[#assign stateName]state${state.gradeType.id}.scoreMarkStyle.id[/#assign]
	            [#if authority!=2 && gradeState.status>0]
	            	${state.gradeType.name!}
	            [#else]
	            	[@b.select items=markStyles value=state.scoreMarkStyle.id?if_exists name=stateName style="width:120px"/]
	            [/#if]
            </td>
            <td>[#if !hasPercentIds?seq_contains(state.gradeType.id)]
            	[#else]
	            	[#if authority!=2 && gradeState.status>0]
	            		[#if state.percent??]${state.percent * 100}[/#if]%
	            	[#else]
	            		<input name="state${state.gradeType.id}.percent" value="[#if state.percent??]${state.percent * 100}[/#if]" style="width:40px"/>%
	            	[/#if]
            	[/#if]
            </td>
            <td>   
            [#if authority!=2 && gradeState.status>0]
             	[#if state.precision==0]保留整数[#else]保留一位小数[/#if]
            [#else]
	            [@b.select name="state${state.gradeType.id}.precision"  style="width:120px;" items={'0':'保留整数','1':'保存一位小数'}/]
	        [/#if]
            </td>
            [#if (state.id)?exists]
            <script>document.collegeGradeEditForm["state${state.gradeType.id}.precision"].value = ${state.precision};</script>
            [/#if]
            <td>
            	<select [#if authority=0 || (authority=1 && state.status=2)]disabled[/#if] name="state${state.gradeType.id}.status" style="width:80px">
            		[#assign publishable = false /]
                	[#list publishableTypes as pgt]
                		[#if pgt.id==state.gradeType.id]
                    		[#assign publishable = true /]
                    	[/#if]
                    [/#list]
                    [#list gradeStatus?keys as key]
	                    [#if (key == "2" && publishable) || key != "2"]
	    	                <option value="${key}"[#if key == state.status?default(-1)?string] selected[/#if]>${gradeStatus[key]}</option>
	                    [/#if]
                    [/#list]
				</select>
            </td>
            <td>${(state.inputedAt?string('yyyy-MM-dd HH:mm'))!}</td>
        </tr>
        [/#list]
    </table>
    <p>各种成绩录入百分比和发布状态,保存时请选中左侧复选框。</p>
	<input type="hidden" name="gradeState.id" value="${gradeState.id!}"/>
	<input type="hidden" name="lesson.id" value="${lesson.id}" />
[/@]