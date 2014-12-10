[#ftl]
[@b.head/]
[@b.grid items=majors var="major"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="代码"]${major.code}[/@]
    [@b.col width="20%" property="name" title="名称"][@b.a href="!info?id=${major.id}"]${major.name}[/@][/@]
    [@b.col width="20%" property="project" title="项目名称"]${major.project.name!}[/@]
    [@b.col width="20%" property="category" title="学科门类"]${major.category.name!}[/@]
    [@b.col width="15%" property="enName" title="英文名"]${major.enName!}[/@]
    [@b.col width="20%" property="beginOn" title="生效时间"]${major.beginOn!}[/@]
    [@b.col width="20%" property="endOn" title="失效时间"]${major.endOn!}[/@]

  [/@]
[/@]
[@b.foot/]
