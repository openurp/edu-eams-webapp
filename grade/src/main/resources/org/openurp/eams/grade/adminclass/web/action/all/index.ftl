[#ftl]
[#include "../components/nav.ftl"]
[#include "/template/macros.ftl"/]
[#if std??]
[@b.form name="allGradeForm" action="!index"]
学生:<select name="std.id">
[#list students as std]
<option value="${std.id}" [#if std.id?string=Parameters['std.id']!]selected[/#if]>${std.code} ${std.name}</option>
[/#list]
</select>
<input name="semester.id" value="${Parameters['semester.id']}" type="hidden"/>
<input name="adminclass.id" value="${adminclass.id}" type="hidden"/>
[@b.submit value="查询"/]
[/@]
<p>${std.code} ${std.name}的全部成绩</p>
[#include "../../../../components/studentGrades.ftl"]
[#else]
该班没有学生。
[/#if]

