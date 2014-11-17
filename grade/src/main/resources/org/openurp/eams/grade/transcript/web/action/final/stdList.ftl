[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="${b.text('std.stdList')}"]
	[#list templates as t]
	[#--
		var m${t_index} = bar.addMenu("${t.name}","stdScorePrint(document.stdListForm,'${t.code}')");
	 	m${t_index}.addItem("导出${t.name}PDF","stdScorePrintToPdf(document.stdListForm,'${t.code}')")
	 --]
	bar.addItem("${t.name}","stdScorePrint(document.stdListForm,'${t.code}')");
	[/#list]
	
	function stdScorePrint(form,template){
		form.target="_blank";
		bg.form.addInput(form,"template",template);
		bg.form.submitId(form,'std.id',true,"${b.url('final!report')}");
	}
	
	function stdScorePrintToPdf(form,template){
		form.target="_blank";
		bg.form.addInput(form,"template",template);
		bg.form.submitId(form,'std.id',true,"${b.url('final!report?format=pdf')}");
	}
	
[/@]
    [#assign stdNameTitle]${b.text('action.info')}${b.text('common.gradeTable')}[/#assign]
    [#include "/components/stdList1stTable.ftl"/]
    [@b.form name="stdListForm"]
		[#include "reportSetting.ftl"/]
    [/@]
[@b.foot/]
