[#ftl]
[@b.head/]
[@b.grid items=teacherStates var="teacherState"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${teacherState.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${teacherState.id}"]${teacherState.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${teacherState.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${teacherState.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${teacherState.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
