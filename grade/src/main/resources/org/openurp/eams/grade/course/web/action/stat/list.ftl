[#ftl]
[@b.head/]
[@b.toolbar title="有教学任务的课程列表"/]
	[@b.grid items=courses var="course" sortable="false"]
		[@b.row]
			[@b.boxcol/]
			[@b.col property="code" title="课程代码" width="15%"/]
			[@b.col property="name" title="课程名称" width="30%"/]
			[@b.col property="category.name" title="课程种类" width="15%"/]
			[@b.col property="credits" title="学分" width="5%"/]
			[@b.col property="project.name" title="教学项目" width="10%"/]
			[@b.col property="education.name" title="entity.education" width="10%"/]
			[@b.col title="操作" width="15%"]<a href="#" onclick="statSetting('${course.id}',this);" title="设置统计的教学日历">设置教学日历</a>[/@]
		[/@]
	[/@]
<script>
	function statSetting(courseId,ele){
		jQuery(ele).colorbox({
			speed: 0,
			href : "${b.url('stat!statSetting')}",
			data : {
						courseIds :courseId
			       }
		});
	}
</script>
[@b.foot/]