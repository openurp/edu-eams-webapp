[#ftl]
[@b.head/]
[@b.toolbar title="批量修改学生标签"]bar.addBack();[/@]
[@b.form action="!saveBatchUpdateLabel" theme="list"]
  [@b.field label="学号"]
    [#list students as s]
      [#if s_index gt 0]，[/#if]${s.person.name}
    [/#list]
   [/@]
  [@b.select2 label="增加学生标签" name1st="addLabelsId1st" name2nd="addLabelsId2nd" 
    items1st=labels items2nd=[] option="id,name"/]
  [@b.select2 label="删除学生标签" name1st="removeLabelsId1st" name2nd="removeLabelsId2nd" 
    items1st=labels items2nd=[] option="id,name"/]
  [@b.formfoot]
    [#list students as s]
        <input type="hidden" name="student.id" value="${s.id}"/>
    [/#list]
    [@b.submit value="action.submit"/]
    [/@]
[/@]
[@b.foot/]