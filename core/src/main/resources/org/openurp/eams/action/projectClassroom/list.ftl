[#ftl]
[@b.head/]
[@b.grid items=projectClassrooms var="projectClassroom"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="name" title="项目名称"][@b.a href="!info?id=${projectClassroom.id}"]${projectClassroom.project.name}[/@][/@]
    [@b.col width="15%" property="room" title="教室"]${projectClassroom.room.name}[/@]
    [@b.col width="20%" property="departs" title="使用部门"]
    [#list (projectClassroom.departs)! as department]
	    ${department.name!}
	    [#if department_has_next]<br>[/#if]
	  [/#list]
      [/@]
  [/@]
[/@]
[@b.foot/]
