[#ftl/]
[@b.head/]
[#include "/template/macros.ftl"/]
[@b.toolbar title="学生列表"]
    bar.addItem("分开打印","printGrade(document.stdListForm)");
    bar.addItem("合并打印","printMultiStdGrade(document.stdListForm)");
    
    function printGrade(form){
       bg.form.submitId(form,"std.id",true,"${b.url('term-report!report')}");
    }
    function printMultiStdGrade(form){
       bg.form.submitId(form,"std.id",true,"${b.url('term-report!multiStdReport')}");
    }
[/@]
  [#include "/components/stdList1stTable.ftl"/]
  [@b.form name="stdListForm" target="_blank"]
  		<input type="hidden" name="semester.id" value="${semesterId}">
  [/@]
[@b.foot/]
