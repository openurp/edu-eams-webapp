[#ftl]
[@b.head/]
[@b.grid items=teacherUnitTypes var="teacherUnitType"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${teacherUnitType.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${teacherUnitType.id}"]${teacherUnitType.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${teacherUnitType.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${teacherUnitType.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${teacherUnitType.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
