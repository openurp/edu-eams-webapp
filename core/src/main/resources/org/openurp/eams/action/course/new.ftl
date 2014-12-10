[#ftl]
[@b.head/]
[@b.toolbar title="新建学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
    [@b.textfield name="course.code" label="代码" value="${course.code!}" required="true" maxlength="20"/]
    [@b.textfield name="course.name" label="名称" value="${course.name!}" required="true" maxlength="20"/]
    [@b.textfield name="course.enName" label="英文名" value="${course.enName!}" maxlength="100"/]
    [@b.select name="course.education.id" label="学历层次" value="${(course.education.id)!}" required="true" 
               style="width:200px;" items=educations option="id,name" empty="..."/]
    [@b.select name="course.category.id" label="课程种类代码" value="${(course.category.id)!}"  
               style="width:200px;" items=categories option="id,name" empty="..."/]
    [@b.textfield name="course.credits" label="学分" value="${course.credits!}" required="true" maxlength="20"/]
    [@b.textfield name="course.period" label="学时" value="${course.period!}" maxlength="100"/]
    [@b.textfield name="course.weekHour" label="周课时" value="${course.weekHour!}" required="true" maxlength="20"/]
    [@b.textfield name="course.weeks" label="周数" value="${course.weeks!}" maxlength="100"/]
    [@b.select name="course.department.id" label="院系" value="${(course.department.id)!}" required="true" 
               style="width:200px;" items=departments option="id,name" empty="..."/]
    [@b.textfield name="course.establishOn" label="设立时间" value="${course.establishOn!}"/]
    [@b.select name="course.courseType.id" label="建议课程类别" value="${(course.courseType.id)!}" required="true" 
               style="width:200px;" items=courseTypes option="id,name" empty="..."/]
    [@b.select name="course.examMode.id" label="考试方式" value="${(course.examMode.id)!}" required="true" 
               style="width:200px;" items=examModes option="id,name" empty="..."/]
    [@b.select name="course.markStyle.id" label="成绩记录方式" value="${(course.markStyle.id)!}" required="true" 
               style="width:200px;" items=markStyles option="id,name" empty="..."/]
    [@b.radios label="课程使用状态"  name="course.enabled" value=course.enabled items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="是否计算绩点"  name="course.calGp" value=course.calGp items="1:common.yes,0:common.no" required="true"/]
    [@b.textfield name="course.remark" label="课程备注" value="${course.remark!}"  maxlength="100"/]
    [@b.select2 label="针对专业" name1st="majorsId1st" name2nd="majorsId2nd" 
      items1st=majors items2nd= course.majors
      option="id,name"/]
    [@b.select2 label="排除专业" name1st="xmajorsId1st" name2nd="xmajorsId2nd" 
      items1st=xmajors items2nd= course.xmajors 
      option="id,name"/]
    [@b.select2 label="先修课程" name1st="prerequisitesId1st" name2nd="prerequisitesId2nd" 
      items1st=prerequisites items2nd= course.prerequisites 
      option="id,name"/]
    [@b.select2 label="小项课程" name1st="subcoursesId1st" name2nd="subcoursesId2nd" 
      items1st=subcourses items2nd= course.subcourses 
      option="id,name"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]