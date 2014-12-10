[#ftl]
[@b.head/]
[@b.grid items=projectPropertys var="projectProperty"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="project" title="项目名称"][@b.a href="!info?id=${projectProperty.id}"]${projectProperty.config.project.name!}[/@][/@]
    [@b.col width="15%" property="config_id" title="项目配置id"]${projectProperty.config.id!}[/@]
    [@b.col width="15%" property="name" title="配置项名称"]${projectProperty.name!}[/@]
    [@b.col width="20%" property="value" title="配置项值"]${projectProperty.value!}[/@]
  [/@]
[/@]
[@b.foot/]
