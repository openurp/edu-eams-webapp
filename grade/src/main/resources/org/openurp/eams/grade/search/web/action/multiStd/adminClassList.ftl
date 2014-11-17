[@ftl/]
[@b.head/]
[@b.toolbar title="班级列表"]
	bar.addItem("查看班级绩点", "adminClassIdAction()", "detail.gif");
    var form = document.actionForm;
    function adminClassIdAction(adminClassId) {
       actionForm.target = "_blank";
   	   form.action = "${b.url('multiStd!classGpaReport')}";
       if (null == adminClassId || "" == adminClassId) {
	       submitId(form, "adminClassId", true);
       } else {
       		addInput(form, "adminClassId", adminClassId, "hidden");
       		form.submit();
       }
		[#--]       
	       bg.form.addInput(form,"pageSize","35");
	       bg.form.addInput(form,"adminclassId",adminClassId);
	       bg.form.submit(form,"multiStdGpa!classGpaReport.action");
	    [--]
    }
[/@]
[#include "/components/adminClassListTable.ftl"/]
[@b.form name="actionForm" target="_blank"/]
[@b.foot/]