[#ftl]
[@b.head/]
[@b.toolbar title="新建学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
    [@b.textfield name="adminclass.code" label="代码" value="${adminclass.code!}" required="true" maxlength="20"/]
    [@b.textfield name="adminclass.name" label="名称" value="${adminclass.name!}" required="true" maxlength="20"/]
    [@b.textfield name="adminclass.abbreviation" label="简称" value="${adminclass.abbreviation!}" maxlength="100"/]
    [@b.startend label="生效失效时间" 
      name="adminclass.beginOn,adminclass.endOn" required="false,false" 
      start=adminclass.beginOn end=adminclass.endOn format="date"/]
    [@b.select name="adminclass.department.id" label="院系" value="${(adminclass.department.id)!}" required="true" 
               style="width:200px;" items=departments option="id,name" empty="..."/]
    [@b.select name="adminclass.major.id" label="专业" value="${(adminclass.major.id)!}" required="true" 
               style="width:200px;" items=majors option="id,name" empty="..."/]
    [@b.select name="adminclass.direction.id" label="方向" value="${(adminclass.direction.id)!}" required="true" 
               style="width:200px;" items=directions option="id,name" empty="..."/]
    [@b.select name="adminclass.stdType.id" label="学生类别" value="${(adminclass.stdType.id)!}" required="true" 
               style="width:200px;" items=stdTypes option="id,name" empty="..."/]

    [@b.select2 label="辅导员" name1st="instructorsId1st" name2nd="instructorsId2nd" 
      items1st=instructors items2nd= adminclass.instructors
      option="id,name"/]
    [@b.select2 label="班导师" name1st="tutorsId1st" name2nd="tutorsId2nd" 
      items1st=tutors items2nd= adminclass.tutors
      option="id,name"/]
    [@b.textfield name="adminclass.remark" label="备注" value="${adminclass.remark!}" maxlength="30"/]
    [@b.textfield name="adminclass.grade" label="年级" value="${adminclass.grade!}" /]
    [@b.textfield name="adminclass.planCount" label="计划人数" value="${adminclass.planCount!}" /]
    [@b.textfield name="adminclass.stdCount" label="学籍有效人数" value="${adminclass.stdCount!}"/]
    
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]