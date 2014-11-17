[#ftl]
[@b.head/]
[@b.toolbar title="学生单科成绩信息"]
	bar.addItem("${b.text('action.save')}","save(document.courseGradeForm)");
	bar.addBack();
	function save(form){
        if(confirm("是否提交修改的成绩?")) {
            bg.form.submit(form);
        }
    }
[/@]
    [#assign formName = "courseGradeForm"/]
    [#assign actionURL = "${b.url('std-grade!save')}"/]
    [#assign inputParamsHTML]
        [#assign filterKeys = ["method", "params"]/]
        <input type="hidden" name="params" value="[#list Parameters?keys as key][#if !filterKeys?seq_contains(key)]&${key}=${Parameters[key]?if_exists}[/#if][/#list]"/>
    [/#assign]
    [#include "../stdGradeFormTable.ftl"/]
[#--
[@b.toolbar title="成绩修改信息"]
	bar.addBlankItem();
[/@]
[#include "../courseGradeAlterInfo.ftl"/]--]
[@b.foot/]
