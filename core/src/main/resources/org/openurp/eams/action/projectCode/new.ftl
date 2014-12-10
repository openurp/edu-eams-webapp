[#ftl]
[@b.head/]
[@b.toolbar title="新建项目基础代码配置"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]

    [@b.select name="projectCode.project.id" label="项目名称" value="${(projectCode.project.id)!}" required="true" 
               style="width:200px;" items=projects option="id,name" empty="..."/]
    [@b.select name="projectCode.meta.id" label="代码元" value="${(projectCode.meta.id)!}" required="true" 
               style="width:200px;" items=metas option="id,name" empty="..."/]
    [@b.textfield name="projectCode.codeId" label="代码ID" value="${projectCode.codeId!}" required="true" maxlength="30"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]


