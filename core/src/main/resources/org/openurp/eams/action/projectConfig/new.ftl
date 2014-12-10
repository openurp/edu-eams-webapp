[#ftl]
[@b.head/]
[@b.toolbar title="新建项目配置"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
    [@b.select name="projectConfig.project.id" label="项目名称" value="${(projectConfig.project.id)!}" required="true" 
               style="width:200px;" items=projects option="id,name" empty="..."/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]


