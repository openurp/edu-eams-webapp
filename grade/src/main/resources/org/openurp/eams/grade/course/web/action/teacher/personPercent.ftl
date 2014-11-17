[#ftl]
[@b.head/]
[#include "/template/macros.ftl"/]
	<center><h2>${student.name}的个人成绩百分比设置</h2>
	<br/>
	课程成绩百分比:[#list gradeTypes?sort_by('code') as gradeType]<input type="hidden" value="${gradeType.id}" name="toInputGradeType.id"/>[#if (gradeState.getState(gradeType).remark)??]${gradeState.getState(gradeType).remark}[#else][@i18nName gradeType/][/#if]&nbsp;[#if (gradeState.getPercent(gradeType)?string.percent)??]<input type="hidden" name="${gradeType.shortName}Percent" value="${gradeState.getPercent(gradeType)*100}"/>(${gradeState.getPercent(gradeType)?string.percent})[/#if][/#list]
	<table  class="gridtable">
		[#list gradeTypes?sort_by('code') as gradeType]
		<tr align="center">
			<td>
				[#if (gradeState.getState(gradeType).remark)??]
					${gradeState.getState(gradeType).remark}
				[#else]
					[@i18nName gradeType/]
				[/#if]
			</td>
			<td>
				<input type="text" role="personPercentSetter" id="Percent_${(gradeType.shortName)!}_${student.id}" value="" /> %
			</td>
		</tr>
		[/#list]
		<tr align="center">
			<td>
				申请理由
			</td>
			<td>
				<textarea id="percent_reason" style="width:180px;height:90px;">[#if (courseGrade.remark)??]${courseGrade.remark?html}[#else]${applyReason!}[/#if]</textarea>
			</td>
		</tr>
		<tr align="center">
			<td colSpan="2">
				<input type="button" value="设置" onClick="setPersonPercentInColorBox()"/>
				<input type="button" value="取消个人百分比" onClick="cancelPersonPercentInColorBox()"/>
			</td>
		</tr>
	</table>
	</center>
<script language="JavaScript">
	jQuery(document).ready(function(){
		jQuery("input[role='personPercentSetter']").each(function(){
			jQuery(this).val(jQuery("input[name='person"+this.id+"']").val());
		})
		jQuery("#percent_reason").val(document.getElementById("courseGrade.remark${student.id}").value);
		jQuery("input[role='personPercentSetter']").blur(function(){
			if(!this.value){
				jQuery(this).val('0');
			}
		})
	})
	function cancelPersonPercentInColorBox(){
		jQuery("input[role='personPercentSetter']").each(function(){
			jQuery("input[name='person"+this.id+"']").val('');
			var inputName = this.id.replace("Percent_","");
			if(!jQuery("input[name="+inputName+"]").hasClass("cannotInput")){
				jQuery("input[name="+inputName+"]").show();
			}
			if(!jQuery("select[name="+inputName+"]").hasClass("cannotInput")){
				jQuery("select[name="+inputName+"]").show();
			}
			jQuery("select[name=examStatus_"+inputName+"]").show();
		})
		var gradeRemark = document.getElementById("courseGrade.remark${student.id}")
		gradeRemark.value = "";
		jQuery(gradeRemark).parent("td").css("background-color","MintCream");
		jQuery.colorbox.close();
	}
	function setPersonPercentInColorBox(){
		var flag = true;
        var total = 0;
		jQuery("input[role='personPercentSetter']").each(function(){
			if(!/^\d+$/.test(this.value)){
				alert("百分比必须为0或正整数");
				flag=false;
				return false;
			}
			total += parseInt(this.value,10);
		})
		if(flag){
			if(total != 100){
				alert("所有设置的百分比数值之和必须是100％。");
				return;
			}
			jQuery("input[role='personPercentSetter']").each(function(){
				var inputName = this.id.replace("Percent_","");
				if(this.value == "0"){
					jQuery("input[name="+inputName+"]").val("").hide();
					jQuery("select[name="+inputName+"]").val("").hide();
					jQuery("select[name=examStatus_"+inputName+"]").hide();
				}else{
					if(!jQuery("input[name="+inputName+"]").hasClass("cannotInput")){
						jQuery("input[name="+inputName+"]").show();
					}
					if(!jQuery("select[name="+inputName+"]").hasClass("cannotInput")){
						jQuery("select[name="+inputName+"]").show();
					}
					jQuery("select[name=examStatus_"+inputName+"]").show();
				}
				jQuery("input[name='person"+this.id+"']").val(this.value);
				jQuery("input[name='"+this.id.replace("Percent_","")+"']").prop("title",this.value+"%");
				jQuery("select[name='"+this.id.replace("Percent_","")+"']").prop("title",this.value+"%");
			})
			var gradeRemark = document.getElementById("courseGrade.remark${student.id}");
			gradeRemark.value = jQuery("#percent_reason").val();
			jQuery(gradeRemark).parent("td").css("background-color","yellow");
			jQuery.colorbox.close();
		}
	}
</script>
[@b.foot/]