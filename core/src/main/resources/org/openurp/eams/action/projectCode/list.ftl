[#ftl]
[@b.head/]
[@b.grid items=projectCodes var="projectCode"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="name" title="项目名称"][@b.a href="!info?id=${projectCode.id}"]${projectCode.project.name!}[/@][/@]
    [@b.col width="20%" property="meta" title="代码元"]${projectCode.meta.name!}[/@]
    [@b.col width="20%" property="codeId" title="代码Id"]${projectCode.codeId!}[/@]
  [/@]
[/@]
[@b.foot/]
