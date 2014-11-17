[#ftl/]
[@b.head/]
[@b.toolbar title="当前学期和历史学期"]
	  bar.addItem("进行绩点重新计算","reStatGp()");
	  function reStatGp(){
		    var form=document.actionForm;
		    form.action="${b.url('!reStatGp')}";
		    form.target="_parent";
		    submitId(form,"semesterId",true);
	  }
[/@]
[@b.grid items=semesters var="semester"]
	[@b.row]
		[@b.col title="semesterId"]${semester.id}[/@]
		[@b.col title="attr.year2year"]${semester.schoolYear}[/@]
		[@b.col title="attr.term"]${semester.name}[/@]
		[@b.col title="起始日期"]${semester.beginOn?string("yyyy-MM-dd")}[/@]
		[@b.col title="结束日期"]${semester.endOn?string("yyyy-MM-dd")}[/@]
	[/@]
[/@]
[@b.form name="actionForm" target=""/]
[@b.foot/]
