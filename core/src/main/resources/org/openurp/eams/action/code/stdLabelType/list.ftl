[#ftl]
[@b.head/]
[@b.grid items=stdLabelTypes var="stdLabelType"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${stdLabelType.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${stdLabelType.id}"]${stdLabelType.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${stdLabelType.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${stdLabelType.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${stdLabelType.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
