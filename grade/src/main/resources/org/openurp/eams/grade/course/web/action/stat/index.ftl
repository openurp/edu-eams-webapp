[#ftl]
[@b.head/]
[#include "nav.ftl" /]
[#include "/template/major3Select.ftl"/]
	[@b.toolbar title="课程成绩统计"]
		bar.addItem("分数段设置", "scoreSectionIndex(document.scoreSectionForm)");
		
        function scoreSectionIndex(form) {
            bg.form.submit(form,"${b.url('stat!scoreSectionIndex')}");
        }
	[/@]
    <table class="indexpanel">
        <tr valign="top">
            <td class="index_view">
            [@b.form theme="search" action="!search" title="ui.searchForm" target="contentDiv" name ="actionForm"]
            	[@b.textfield label="课程代码" name="lesson.course.code" value="" maxlength="30"/]
            	[@b.textfield label="课程名称" name="lesson.course.name" value="" maxlength="50"/]
				[@majorSelect id="s1" projectId="lesson.project.id" educationId="lesson.course.education.id" departId="lesson.teachClass.depart.id" majorId="lesson.teachClass.major.id" directionId="lesson.teachClass.direction.id" /]
            [/@]
            </td>
           	<td class="index_content">
                [@b.div id="contentDiv" href="!search?lesson.semester.id=${(semester.id)?default('')}"/]
            </td>
        </tr>    
    </table>  
    [@b.form name="scoreSectionForm" target="contentDiv"/]
[@b.foot/]
