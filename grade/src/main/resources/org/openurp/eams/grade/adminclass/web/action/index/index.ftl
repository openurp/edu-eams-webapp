[#ftl/]
[@b.head/]
[@b.toolbar title="班级成绩查看"/]
[#if adminclasses?size=0]
	系统中还没有你带的班级.
[#else]
[#assign adminclass=adminclasses?first/]
[#list adminclasses as adc][#if adc.id?string=Parameters['adminclass.id']!] [#assign adminclass=adc/][/#if][/#list]
<script>
	function changeadminclass(id){
		bg.Go('${b.url("!index?adminclass.id=")}'+id,bg.findTarget(document.getElementById('instructor_adminclass_id')));
	}
</script>
[@eams.semesterBar name="project.id" semesterEmpty=false semesterName="semester.id" semesterValue=semester]
	班级:[@b.select name="adminclass.id" id="instructor_adminclass_id" items=adminclasses style="width:150px" value=adminclass  onchange='changeadminclass(this.value)'/]
[/@]
[@b.div id="grade_content_div"]
	[@b.navmenu]
		[@b.navitem title="班级信息" href="index?adminclass.id=${adminclass.id}" selected=true/]
		[@b.navitem title="学期成绩" href="term?adminclass.id=${adminclass.id}&semester.id=${semester.id}&notprint=1" /]
		[@b.navitem title="学业警告" href="alert?adminclass.id=${adminclass.id}&semester.id=${semester.id}"/]
		[@b.navitem title="总成绩" href="all?adminclass.id=${adminclass.id}&semester.id=${semester.id}" /]
	[/@]
[#include "../components/classinfo.ftl"/]
[/@]
[/#if]
[@b.foot/]