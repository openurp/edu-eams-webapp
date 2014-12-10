[#ftl]
[@b.head/]
[@b.toolbar title="修改学籍状态日志"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!update?id=${studentJournal.id}" theme="list"]
    [@b.textfield name="studentJournal.grade" label="年级" value="${studentJournal.grade!}" required="true" /]    
    [@b.select name="studentJournal.department.id" label="行政管理院系" value="${(studentJournal.department.id)!}" required="true" 
               style="width:200px;" items=departments option="id,name" empty="..."/]    
    [@b.select name="studentJournal.major.id" label="专业" value="${(studentJournal.major.id)!}" required="true" 
               style="width:200px;" items=majors option="id,name" empty="..."/]    
    [@b.select name="studentJournal.direction.id" label="专业方向" value="${(studentJournal.direction.id)!}" required="true" 
               style="width:200px;" items=directions option="id,name" empty="..."/]    
    [@b.radios label="是否在校"  name="studentJournal.inschool" value=studentJournal.inschool items="1:common.yes,0:common.no"/]
    [@b.textfield name="studentJournal.remark" label="备注" value="${studentJournal.remark!}"/]
    [@b.select name="studentJournal.status.id" label="学籍状态" value="${(studentJournal.status.id)!}" required="true" 
               style="width:200px;" items=statuses option="id,name" empty="..."/]
    [@b.select name="studentJournal.adminclass.id" label="行政班级" value="${(studentJournal.adminclass.id)!}" required="true" 
               style="width:200px;" items=adminclasses option="id,name" empty="..."/]
    [@b.startend label="生效失效日期" 
      name="studentJournal.beginOn,studentJournal.endOn" required="false,false" 
      start=studentJournal.beginOn end=studentJournal.endOn format="date"/]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]

[@b.foot/]