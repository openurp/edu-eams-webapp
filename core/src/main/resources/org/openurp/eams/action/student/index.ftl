[#ftl]
[@b.head/]
[@b.toolbar title="学籍信息"/]
<table class="indexpanel">
  <tr>
    <td class="index_view">
    [@b.form name="studentSearchForm" action="!search" target="studentlist" title="ui.searchForm" theme="search"]
      [@b.textfields names="student.code;学号"/]
      [@b.textfields names="student.person.name;姓名"/]
      [@b.select name="student.majorDepart.id" label="院系" items=majorDeparts option="id,name"  empty="..." style="width:100px"/]
      [@b.select name="student.major.id" label="专业" items=majors option="id,name"  empty="..." style="width:100px"/]
      [@b.select name="student.person.nation.id" label="民族" items=nations option="id,name"  empty="..." style="width:100px"/]
      [@b.select name="student.person.gender.id" label="性别" items=genders option="id,name"  empty="..." style="width:100px"/]
      [@b.select name="stdLabelId" label="标签" items=labels option="id,name"  empty="..." style="width:100px"/]
      <input type="hidden" name="orderBy" value="student.code"/>
    [/@]
    </td>
    <td class="index_content">[@b.div id="studentlist" href="!search?orderBy=student.code"/]
    </td>
  </tr>
</table>
[@b.foot/]