[#ftl]
[@b.head/]
[@b.grid items=examModes var="examMode"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${examMode.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${examMode.id}"]${examMode.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${examMode.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${examMode.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${examMode.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
