[#ftl]
[@b.head/]
[@b.toolbar title="修改项目"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!update?id=${project.id}" theme="list"]
    [@b.textfield name="project.name" label="名称" value="${project.name!}" required="true" maxlength="20"/]
    [@b.select name="project.school.id" label="适用学校" value="${(project.school.id)!}" required="true" 
               style="width:200px;" items=schools option="id,name" empty="..."/]
    [@b.startend label="生效失效时间" 
      name="project.beginOn,project.endOn" required="false,false" 
      start=project.beginOn end=project.endOn format="date"/]
    [@b.textfield name="project.description" label="描述" value="${project.description!}" maxlength="30"/]
    [@b.radios label="是否辅修"  name="project.minor" value=project.minor items="1:common.yes,0:common.no"/]
    [@b.select name="project.calendar.id" label="使用校历" value="${(project.calendar.id)!}" required="true" 
               style="width:200px;" items=calendars option="id,name" empty="..."/]
    [@b.select2 label="校区列表" name1st="campusesId1st" name2nd="campusesId2nd" 
      items1st=campuses items2nd= project.campuses
      option="id,name"/]
    [@b.select2 label="部门列表" name1st="departmentsId1st" name2nd="departmentsId2nd" 
      items1st=departments items2nd= project.departments 
      option="id,name"/]
    [@b.select2 label="学历层次列表" name1st="educationsId1st" name2nd="educationsId2nd" 
      items1st=educations items2nd= project.educations 
      option="id,name"/]
    [@b.select2 label="学生分类列表" name1st="labelsId1st" name2nd="labelsId2nd" 
      items1st=labels items2nd= project.labels 
      option="id,name"/]
    [@b.select2 label="学生类别列表" name1st="typesId1st" name2nd="typesId2nd" 
      items1st=types items2nd= project.types 
      option="id,name"/]
    [@b.select2 label="小节设置" name1st="timeSettingsId1st" name2nd="timeSettingsId2nd" 
      items1st=timeSettings items2nd= project.timeSettings 
      option="id,name"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]