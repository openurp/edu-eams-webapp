[#ftl/]
[@b.head/]
[#include "nav.ftl" /]
[#include "/template/major3Select.ftl"/]
[@b.toolbar title="${b.text('common.stdPersonGradeTable')}"/]
    <table class="indexpanel">
        <tr valign="top">
            <td class="index_view">
            [@b.form theme="search" action="!stdList" title="ui.searchForm" target="contentDiv" name ="actionForm"]
            	<input type="hidden" name="orderBy" value="std.code asc"/>
            	[@b.textfield label="attr.stdNo" name="std.code" value="" maxlength="32" /]
            	[@b.textfield label="attr.personName" name="std.name" value="" maxlength="20" /]
            	[@b.textfield label="年级" name="std.grade" value="" maxlength="30"/]
				[@majorSelect id="s1" projectId="std.project.id" educationId="std.education.id" departId="std.department.id" majorId="std.major.id" directionId="std.direction.id" stdTypeId="std.type.id"/]
				[@b.textfield label="common.adminClass" name="adminclassName" maxlength="20"/]
				[@b.select label="是否有效" name="stdActive" items={'':'全部','1':'有效','0':'无效'} value="1"/]
            [/@]
            </td>
           	<td class="index_content">
                [@b.div id="contentDiv" href="!stdList?stdActive=1&orderBy=std.code"/]
            </td>
        </tr>    
    </table>  
[@b.foot/]
