[#ftl/]
[#include "/template/macros.ftl"/]
[@b.head/]
	[@b.toolbar title="已统计学生绩点且有绩点的专业列表"/]
	[@b.grid items=majors var="major"]
		[@b.gridbar]
			bar.addItem("查看排名", "showOnCourse(document.actionForm)");
	        
	        function showOnCourse(form){
	    		bg.form.submitId(form,"major.id",false,"${b.url('!majorRanking')}");
	    		
	        }
	        
	        function ranking(dataId,form) {
	        	bg.form.addInput(form,"majorId",dataId);
	        	bg.form.submit(form,"${b.url('!majorRanking')}");
	        }
		[/@]
		[@b.row]
			[@b.boxcol/]
			[@b.col title="attr.code" property="code" width="10%"/]
			[@b.col title="common.name" property="name" width="20%"][#--<a href="#" onclick="ranking('${major.id}',document.actionForm)">--][@i18nName major/][/@]
			[@b.col title="${b.text('education')}" width="10%"][#list major.educations?if_exists as education][@i18nName education/][#if education_has_next],[/#if][/#list][/@]
			[@b.col title="院系" width="20%"][@i18nName major.department/][/@]
			[@b.col title="学科(门类)" width="20%"]${(major.subject.name)!}[#if (major.subject.category)??](${(major.subject.category.name)!})[/#if][/@]
			[@b.col title="${b.text('project')}" property="project.name" width="20%"/]
		[/@]
	[/@]
	[@b.form target="pageIframe" name="actionForm"/]
[@b.foot/]