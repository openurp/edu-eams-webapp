[#ftl/]
[@b.head/]
[#include "nav.ftl" /]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="学生每学期成绩表"]
	bar.addItem("统计学分不过半学生", "lessHalfStat('')");
[/@]
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester/]
<table class="indexpanel">
	<tr>
		<td class="index_view">
		[@b.form name="stdSearchForm" action="!stdList" title="ui.searchForm" target="contentDiv" theme="search"]
			<input type="hidden" name="semester.id" value="${semester.id}"/>
			<input type="hidden" name="orderBy" value="std.code"/>
		    [@b.textfield label="${b.text('attr.stdNo')}" name="std.code" value="" maxlength="32" /]
        	[@b.textfield label="${b.text('attr.personName')}" name="std.name" value="" maxlength="20" /]
        	[@b.textfield label="std.grade" name="std.grade" value="" maxlength="30"/]
			[@majorSelect id="s1" projectId="std.project.id" educationId="std.education.id" departId="std.department.id" majorId="std.major.id" directionId="std.direction.id" stdTypeId="std.type.id"/]
			[@b.textfield label="entity.class" name="adminClassName" maxlength="20"/]
			[@b.select label="是否有效" name="stdActive" items={'':'全部','1':'有效','0':'无效'} value="1"/]
		[/@]
	   	</td>
		<td class="index_content">
			[@b.div id="contentDiv" href="!stdList?semester.id=${semester.id}&orderBy=std.code" /]
		</td>
	</tr>
</table>
<script>

    function lessHalfStat() {
	   	var form =document.stdSearchForm;
	   	form.target = "_blank";
	   	bg.form.submit(form,"${b.url('term-report!lessHalfStat')}");
	   	form.target="contentDiv";
    }
</script> 
	[@b.foot/]
