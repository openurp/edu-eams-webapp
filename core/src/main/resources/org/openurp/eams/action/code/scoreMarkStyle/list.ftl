[#ftl]
[@b.head/]
[@b.grid items=scoreMarkStyles var="scoreMarkStyle"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${scoreMarkStyle.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${scoreMarkStyle.id}"]${scoreMarkStyle.name}[/@][/@]
    [@b.col width="15%" property="enName" title="英文名"]${scoreMarkStyle.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${scoreMarkStyle.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${scoreMarkStyle.endOn!}[/@]
  [/@]
[/@]
[@b.foot/]
