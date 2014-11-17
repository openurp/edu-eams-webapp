[#ftl]
[@b.head/]
[@b.toolbar title="成绩录入开关设置"]
	bar.addBack();
[/@]
   	[@b.form name="gradeSwEditForm" title="设置录入开关" action="!save" theme="list"]
        [@eams.semesterCalendar label="学年学期" name="gradeInputSwitch.semester.id" value=gradeInputSwitch.semester/]
		[@b.datepicker readOnly="readOnly" label="开始时间" required="true" id="effectiveAt" name="gradeInputSwitch.startAt" value="${(gradeInputSwitch.startAt?string('yyyy-MM-dd HH:mm'))?default('')}" style="width:200px"  format="yyyy-MM-dd HH:mm" maxDate="#F{$dp.$D(\\'invalidAt\\')}"/]
		[@b.datepicker readOnly="readOnly" label="结束时间" required="true" id="invalidAt" name="gradeInputSwitch.endAt" value="${(gradeInputSwitch.endAt?string('yyyy-MM-dd HH:mm'))?default('')}" style="width:200px" format="yyyy-MM-dd HH:mm" minDate="#F{$dp.$D(\\'effectiveAt\\')}"/]
		[@b.field label="成绩类型"]
			<table>
				<tr>
					<td>
					<select name="gradeTypes" multiple="multiple" size="20" style="width:200px;height:300px" onDblClick="JavaScript:bg.select.moveSelected(this.form['gradeTypes'], this.form['SelectedGradeType'])" >
						[#list gradeTypes?sort_by('code') as canInputType]
							[#if gradeInputSwitch.types?seq_contains(canInputType)]
							[#else]
							<option value="${(canInputType.id)!}">${(canInputType.name)!}</option>
							[/#if]
						[/#list]
					</select>
					</td>
					<td style="width:30px">
						<input style="margin:auto" onclick="JavaScript:bg.select.moveSelected(this.form['gradeTypes'], this.form['SelectedGradeType'])" type="button" value="&gt;"/>
						<input style="vertical-align: middle;" onclick="JavaScript:bg.select.moveSelected(this.form['SelectedGradeType'], this.form['gradeTypes'])" type="button" value="&lt;"/>
					</td>
					<td>
					<select name="SelectedGradeType" multiple="multiple" size="20" style="width:200px;height:300px" onDblClick="JavaScript:bg.select.moveSelected(this.form['SelectedGradeType'], this.form['gradeTypes'])">
						[#list gradeInputSwitch.types?if_exists?sort_by('code') as mngGradeType]
						<option value="${(mngGradeType.id)!}">${(mngGradeType.name)!}</option>
						[/#list]
					</select>
					</td>
				</tr>
			</table>
		[/@]   
		[@b.radios name="gradeInputSwitch.opened" label="是否开放" required="true" value="${gradeInputSwitch.opened?if_exists?string('1','0')}"/]
		[@b.radios name="gradeInputSwitch.needValidate" label="是否开启邮箱认证" required="true" value="${gradeInputSwitch.needValidate?string('1','0')}"/]
		[@b.textarea name="gradeInputSwitch.remark" style="width:300px;height:100px" label="录入提示" check="maxLength(200)" value=(gradeInputSwitch.remark?html)!/] 
  		[@b.formfoot]
  			<input type="hidden" name="gradeInputSwitch.id" value="${(gradeInputSwitch.id)?if_exists}"/>
			
			[@b.submit value="action.submit" onsubmit="addOptionValues"/]&nbsp;
			<input type="reset"  name="reset1" value="${b.text("action.reset")}" class="buttonStyle" />
		[/@]
   [/@]
   <script language="JavaScript">
		function getAllOptionValue(select){
			var val = "";
			var options = select.options;
			for (var i=0; i<options.length; i++){   
				if (val != ""){
					val = val + ",";
				}	
				val = val + options[i].value;
			}
			return val;
		}
		
		function addOptionValues(form){
			bg.form.addInput(form,"gradeTypeIds",getAllOptionValue(form["SelectedGradeType"]));
			return true;
		}
   </script>
[@b.foot/]
