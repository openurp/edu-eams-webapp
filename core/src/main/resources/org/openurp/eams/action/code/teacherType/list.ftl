[#ftl]
[@b.head/]
[@b.grid items=teacherTypes var="teacherType"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${teacherType.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${teacherType.id}"]${teacherType.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${teacherType.enName!}[/@]
    [@b.col width="10%" property="parttime" title="是否兼职"]${(teacherType.parttime?string("是","否"))!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${teacherType.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${teacherType.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
