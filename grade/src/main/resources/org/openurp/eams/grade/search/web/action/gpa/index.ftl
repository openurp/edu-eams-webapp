[#ftl/]
[#include "/template/macros.ftl"/]
[#include "/template/major3Select.ftl"/]
[@b.div id="allStat"]
[@b.head/]
	[@b.toolbar title="学生个人绩点管理"]
	    bar.addItem("未统计学生名单","reminderStds(document.stdSearchForm)");
	    bar.addItem("统计班级排名","adminclassIndex(document.stdSearchForm)");
	    bar.addItem("统计专业排名","majorIndex(document.stdSearchForm)");
	    
	     
	    function reminderStds(form){
	    	form.target="_blank";
	    	bg.form.submit(form,"${b.url('!reminderStds')}");
	    	form.target="stdLisForm";
	    }
	    
	    function adminclassIndex(form) {
	        form.target="allStat";
	        bg.form.submit(form,"${b.url('!adminclassIndex')}");
	        form.target="stdLisForm";
	    }
	    
	    function majorIndex(form) {
	        form.target="allStat";
			bg.form.submit(form,"${b.url('!majorIndex')}");	  
	        form.target="stdLisForm";
	    }
	[/@]
			<table class="indexpanel">
					<tr>
						<td class="index_view">
							[@b.form name="stdSearchForm" action="!search" title="ui.searchForm" target="stdLisForm" theme="search"]
								[@b.textfield label="attr.stdNo" name="stdGpa.std.code" value="${Parameters['std.code']?if_exists}" maxlength="32"/]
								[@b.textfield label="attr.personName" name="stdGpa.std.name" value="${Parameters['std.name']?if_exists}" maxlength="20"/]
								[@b.textfield label="年级" name="stdGpa.std.grade" maxlength="7"/]
								[@majorSelect id="s1" projectId="stdGpa.std.project.id" educationId="stdGpa.std.education.id" departId="stdGpa.std.department.id" majorId="stdGpa.std.major.id" directionId="stdGpa.std.direction.id" stdTypeId="stdGpa.std.stdType.id"/]							
								[@b.textfield label="common.adminClass" name="stdGpa.std.adminclass.name" value="" maxlength="20"/]
								[#--]
								[@b.select label="是否在籍" name="stdGpa.std.active" items={'':'全部','1':'有效','0':'无效'}   value="1"/]
								[@b.select label="是否在校" name="stdGpa.std.inSchool" items={'':'全部','1':'有效','0':'无效'} value="1"/]
								[--]
							[/@]
						</td>
						<td class="index_content">
							[@b.div id="stdLisForm" href="!search" /]
						</td>
					</tr>
			<table>
[@b.foot/]
[/@]
