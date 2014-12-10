[#ftl]
[@b.head/]
[@b.toolbar title="新建项目教室配置"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]

    [@b.select name="projectClassroom.project.id" label="项目名称" value="${(projectClassroom.project.id)!}" required="true" 
               style="width:200px;" items=projects option="id,name" empty="..."/]
    [@b.select name="projectClassroom.room.id" label="教室" value="${(projectClassroom.room.id)!}" required="true" 
               style="width:200px;" items=rooms option="id,name" empty="..."/]
    [@b.select2 label="使用部门" name1st="departsId1st" name2nd="departsId2nd" 
      items1st=departs items2nd= projectClassroom.departs
      option="id,name"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]


