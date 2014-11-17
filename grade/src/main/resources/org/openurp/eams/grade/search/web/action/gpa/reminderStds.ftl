[#ftl/]
[#include "/template/macros.ftl"/]
[@b.head/]
[@b.toolbar title="学生列表"]
	bar.addItem("统计平均绩点","stdGpReport(document.stdListForm)");
	bar.addClose();
	
    function stdGpReport(form){
       bg.form.submitId(form,"std.id",true,"${b.url('!stdGpaReport')}");
       form.action="${b.url('!reminderStds')}";
    }
[/@]
  [@b.form name="stdListForm" method="post" action="!reminderStds"]
  	 [@b.grid items=students var="std" filterable="true"]
	 	[@b.row]
	 		[@b.boxcol/]
	 		[@b.col title="attr.stdNo" property="code" width="10%"]
	 			[@b.a href="/studentSearch!info?studentId=${std.id}" target="_blank" title="查看学生基本信息"]${(std.code)?if_exists}[/@]
	 		[/@]
	 		[@b.col title="attr.personName" property="name" width="10%"]
	 			[@b.a href="!stdGpaReport?stdIds=${std.id}" target="_blank" title="查看每学期绩点"]${std.name}[/@]
	 		[/@]
	 		[@b.col title="entity.gender" property="gender.id" width="5%"][@i18nName std.gender?if_exists/][/@]
	 		[@b.col title="std.grade" property="grade" width="5%"/]
	 		[@b.col title="entity.department" property="department.name" width="20%"][@i18nName std.department?if_exists/][/@]
	 		[@b.col title="entity.major" property="major.name" width="30%"][@i18nName std.major?if_exists/][/@]
	 		[@b.col title="entity.direction" property="direction.name" width="20%"][@i18nName std.direction?if_exists/][/@]
	 	[/@]
	 [/@]
  [/@]
[@b.foot/]
