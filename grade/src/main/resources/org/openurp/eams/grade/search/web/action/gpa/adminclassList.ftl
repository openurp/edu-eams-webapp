[#ftl/]
[#include "/template/macros.ftl"/]
[@b.head/]
[@b.toolbar title="已统计学生绩点且有绩点的班级列表"/]
	 [@b.grid items=adminclasses var="adminclass"]
		 	[@b.gridbar]
				bar.addItem("查看排名", "showOnCourse(document.adminclassForm)");
				
		        function showOnCourse(form){
		    		bg.form.submitId(form,"adminclass.id",false,"${b.url('!adminclassRanking')}");
		        }
		        
		        function ranking(dataId,form) {
		        	bg.form.addInput(form,"adminclass.id",dataId);
		        	bg.form.submit(form,"${b.url('!adminclassRanking')}");
		        }
		 	[/@]
	 	[@b.row]
	 		[@b.boxcol/]
	 		[@b.col property="grade" title="std.grade"/]
	 		[@b.col property="name" title="attr.name"]
	 			<a href="#" onclick="ranking(${adminclass.id},document.adminclassForm)">[@i18nName adminclass/]</a>
	 		[/@]
	 		[@b.col property="stdType.name" title="entity.studentType"/]
	 		[@b.col property="department.name" title="entity.department"/]
	 		[@b.col property="major.name" title="entity.major"/]
	 		[@b.col property="direction.name" title="entity.direction"/]
	 	[/@]
 [/@]
 [@b.form name="adminclassForm" target="pageIframe"/]
[@b.foot/]

