[#ftl]
[@b.head/]
[@b.toolbar title="新建学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
  
    [@b.radios label="是否通过"  name="courseHour.passed" value=courseHour.passed items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="是否提交"  name="courseHour.beyondSubmit" value=courseHour.beyondSubmit items="1:common.yes,0:common.no" required="true"/]
    [@b.select name="courseHour.gradeType.id" label="成绩类型" value="${(courseHour.gradeType.id)!}" required="true" 
               style="width:200px;" items=gradeTypes option="id,name" empty="..."/]
    [@b.select name="courseHour.markStyle.id" label="成绩记录方式" value="${(courseHour.markStyle.id)!}" required="true" 
               style="width:200px;" items=markStyles option="id,name" empty="..."/]
    [@b.select name="courseHour.examStatus.id" label="考试情况" value="${(courseHour.examStatus.id)!}" required="true" 
               style="width:200px;" items=examStatuses option="id,name" empty="..."/]
    [@b.select name="courseHour.courseHour.id" label="对应的课程成绩" value="${(courseHour.courseHour.id)!}" required="true" 
               style="width:200px;" items=courseHours option="id,name" empty="..."/]
    [@b.textfield name="courseHour.score" label="得分" value="${courseHour.score!}" maxlength="30"/]
    [@b.textfield name="courseHour.scoreText" label="得分字面值" value="${courseHour.scoreText!}" /]
    [@b.textfield name="courseHour.status" label="成绩状态" value="${courseHour.status!}" /]
    [@b.textfield name="courseHour.operator" label="操作者" value="${courseHour.operator!}"/]
    [@b.textfield name="courseHour.percent" label="百分比描述" value="${courseHour.percent!}"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]