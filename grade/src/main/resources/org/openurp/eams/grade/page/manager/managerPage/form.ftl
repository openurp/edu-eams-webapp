[#ftl]
[@b.head/]
[@b.toolbar title="学生单科成绩信息"]
	bar.addItem("${b.text('action.save')}","save(document.courseGradeForm)");
	bar.addBack();
[/@]    
    [#assign formName = "courseGradeForm"/]
    [#assign actionURL = "!save"/]
    [#assign inputParamsHTML]
        <input type="hidden" name="courseGrade.id" value="${courseGrade.id}"/>
    [/#assign]
    [#include "../common/stdGradeFormTable.ftl"/]
 
    <script>
        function save(form){
            if (confirm("是否提交修改的成绩?")) {
            	bg.form.submit(form,form.ation);
            }
        }
    </script>
[@b.foot/]
