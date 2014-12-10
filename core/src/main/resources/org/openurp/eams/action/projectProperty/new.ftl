[#ftl]
[@b.head/]
[@b.toolbar title="新建项目配置属性"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]

    [@b.textfield name="projectProperty.name" label="配置项名称" value="${projectProperty.name!}" required="true" maxlength="30"/]
    [@b.textfield name="projectProperty.value" label="配置项值" value="${projectProperty.value!}" required="true" maxlength="30"/]
    [@b.select name="projectProperty.config.id" label="项目配置" value="${(projectProperty.config.id)!}" required="true" 
               style="width:200px;" items=configs option="id,name" empty="..."/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]


