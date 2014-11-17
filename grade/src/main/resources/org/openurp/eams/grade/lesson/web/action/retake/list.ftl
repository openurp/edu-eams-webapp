[#ftl]
<script>
	function freespace(id){
		bg.Go('${b.url("!freespace?semester.id=${Parameters['semester.id']}&course.id=")}'+id,'freespace_lessons');
		jQuery.colorbox({
		 overClose : false,
		 inline:true,
		 transition:'none',
		 title:'剩余开课容量',
		 href:'#freespace_lessons'
		});
	}
	function unpassed(id){
		bg.Go('${b.url("!unpassed?orderBy=grade.std.code&course.id=")}'+id,'freespace_lessons');
		jQuery.colorbox({
		 overClose : false,
		 inline:true,
		 transition:'none',
		 title:'不及格成绩',
		 href:'#freespace_lessons'
		});
	}
</script>

[@b.grid items=stats var="stat"]
	[@b.gridbar]
		bar.addItem("生成重修任务",action.multi("newLesson","确定生成重修任务","semester.id=${Parameters['semester.id']}"));
	[/@]
	[@b.row]
		[@b.boxcol property="course.id" width="5%" boxname="stat.id"/]
		[@b.col property="course.code" title="课程代码" width="10%"/]
		[@b.col property="course.name" title="课程名称" width="35%"/]
		[@b.col property="course.department.name" title="开课院系" width="20%"/]
		[@b.col property="newspace" title="需增开人数" width="10%"/]
		[@b.col property="unpassed" title="不及格人数" width="10%"]<a title="查看不及格成绩" href="#" onclick="unpassed(${stat.course.id})">${stat.unpassed}</a>[/@]
		[@b.col property="freespace" title="剩余开课容量" width="10%"]<a title="查看有剩余容量的任务" href="#" onclick="freespace(${stat.course.id})">${stat.freespace}</a>[/@]
	[/@]
[/@]
<div style="display:none">
	[@b.div id="freespace_lessons" style="width:1000px;height:400px"/]
</div>
