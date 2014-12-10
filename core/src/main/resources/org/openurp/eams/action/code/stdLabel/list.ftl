[#ftl]
[@b.head/]
[@b.grid items=stdLabels var="stdLabel"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${stdLabel.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${stdLabel.id}"]${stdLabel.name}[/@][/@]
    [@b.col width="10%" property="labelType" title="标签类型"]${(stdLabel.labelType.name)!}[/@]
    [@b.col width="15%" property="enName" title="英文名"]${stdLabel.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${stdLabel.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${stdLabel.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
