[#ftl]
[@b.head/]
<script language="JavaScript" type="text/JavaScript" src="${base}/static/scripts/course/grade/gradeSeg.js"></script>
<div style="padding-left:20px">
<p >${lesson.course.name}(${lesson.no})成绩提交成功!</p>
[@b.a href="!report?lessonId=${lesson.id}&gradeTypeIds=${Parameters['gradeTypeIds']}" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"]<span class="ui-button-text">打印报表</span>[/@]
[@ems.guard res="/teach/grade/course/teach-quality"]
	<a href="#" onClick="stat();" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only">
		<span class="ui-button-text">成绩分段统计</span>
	</a>
[/@]
</div>
	[@b.form name="gradeReportForm" target="_self"]
		<input type="hidden" name="lesson.id" value="${lesson.id}" />
	[/@]
	<script>
		function stat(){
			var form = document.gradeReportForm;
		    for(var i=0;i<seg.length;i++){
	          var segAttr="segStat.scoreSegments["+i+"]";
	          bg.form.addInput(form,segAttr+".min",seg[i].min);
	          bg.form.addInput(form,segAttr+".max",seg[i].max);
	        }
	        bg.form.addInput(form,"kind","task");
	        bg.form.addInput(form,"scoreSegmentsLength",seg.length);
	        bg.form.submit(form,"${base}/teach/grade/lesson/report!stat.action");
		 }
	</script>
[@b.foot/]