[#ftl]
[@b.head/]
[@b.toolbar title="新建专业建设过程"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
  [#-- 
    [@b.textfield name="majorJournal.code" label="代码" value="${majorJournal.code!}" required="true" maxlength="20"/]
    [@b.textfield name="majorJournal.name" label="名称" value="${majorJournal.name!}" required="true" maxlength="20"/]
    [@b.textfield name="majorJournal.enName" label="英文名" value="${majorJournal.enName!}" maxlength="100"/]
   --]
    [@b.startend label="生效失效时间" 
      name="majorJournal.beginOn,majorJournal.endOn" required="false,false" 
      start=majorJournal.beginOn end=majorJournal.endOn format="date"/]
    [@b.textfield name="majorJournal.remark" label="备注" value="${majorJournal.remark!}" maxlength="30"/]
    [@b.select name="majorJournal.major.id" label="专业" value="${(majorJournal.major.id)!}" required="true" 
               style="width:200px;" items=majors option="id,name" empty="..."/]       
    [@b.select name="majorJournal.category.id" label="学科门类" value="${(majorJournal.category.id)!}" required="true" 
               style="width:200px;" items=categories option="id,name" empty="..."/]     
    [@b.select name="majorJournal.education.id" label="培养层次" value="${(majorJournal.education.id)!}" required="true" 
               style="width:200px;" items=educations option="id,name" empty="..."/]
    [@b.select name="majorJournal.depart.id" label="部门" value="${(majorJournal.depart.id)!}" required="true" 
               style="width:200px;" items=departs option="id,name" empty="..."/]
    [@b.textfield name="majorJournal.duration" label="修读年限" value="${majorJournal.duration!}" required="true"/]
    [@b.textfield name="majorJournal.disciplineCode" label="教育部代码" value="${majorJournal.disciplineCode!}" required="true"/]
               
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]