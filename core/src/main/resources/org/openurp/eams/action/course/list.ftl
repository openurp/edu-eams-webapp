[#ftl]
[@b.head/]
[@b.grid items=courses var="course"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${course.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${course.id}"]${course.name}[/@][/@]
    [@b.col width="15%" property="credits" title="学分"]${course.credits!}[/@]
    [@b.col width="15%" property="department" title="院系"]${(course.department.name)!}[/@]
  [/@]
  [/@]
[@b.foot/]
