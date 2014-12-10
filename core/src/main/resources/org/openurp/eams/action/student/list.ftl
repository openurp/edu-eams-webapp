[#ftl]
[@b.head/]
[@b.grid items=students var="student"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    [#-- bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));--]
    bar.addItem("批量修改标签",action.multi("batchUpdateLabel?_method=put"));
    bar.addItem("批量指定标签学生",action.method("batchInputLabel"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="学号"][@b.a href="!info?id=${student.id}"]${student.code}[/@][/@]
    [@b.col width="20%" property="person.name" title="姓名"][@b.a href="!info?id=${student.id}"]${(student.person.name)!}[/@][/@]
    [@b.col width="15%" property="grade" title="年级"/]
    [@b.col width="15%" property="major" title="专业"]${(student.major.name)!}[/@]
    [@b.col width="10%" property="department" title="院系"]${(student.department.name)!}[/@]
    [@b.col width="10%" property="person.gender.name" title="性别"/]
    [@b.col width="10%" title="标签"]
      [#list (student.labels.values)! as label]
        [#if label_index gt 0]，[/#if]${label.name}
      [/#list]
      [/@]
  [/@]
[/@]
[@b.foot/]
