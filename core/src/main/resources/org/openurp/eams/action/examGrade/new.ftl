[#ftl]
[@b.head/]
[@b.toolbar title="新建学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
  
    [@b.radios label="是否通过"  name="examGrade.passed" value=examGrade.passed items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="是否提交"  name="examGrade.beyondSubmit" value=examGrade.beyondSubmit items="1:common.yes,0:common.no" required="true"/]
    [@b.select name="examGrade.gradeType.id" label="成绩类型" value="${(examGrade.gradeType.id)!}" required="true" 
               style="width:200px;" items=gradeTypes option="id,name" empty="..."/]
    [@b.select name="examGrade.markStyle.id" label="成绩记录方式" value="${(examGrade.markStyle.id)!}" required="true" 
               style="width:200px;" items=markStyles option="id,name" empty="..."/]
    [@b.select name="examGrade.examStatus.id" label="考试情况" value="${(examGrade.examStatus.id)!}" required="true" 
               style="width:200px;" items=examStatuses option="id,name" empty="..."/]
    [@b.select name="examGrade.courseGrade.id" label="对应的课程成绩" value="${(examGrade.courseGrade.id)!}" required="true" 
               style="width:200px;" items=courseGrades option="id,name" empty="..."/]
    [@b.textfield name="examGrade.score" label="得分" value="${examGrade.score!}" maxlength="30"/]
    [@b.textfield name="examGrade.scoreText" label="得分字面值" value="${examGrade.scoreText!}" /]
    [@b.textfield name="examGrade.status" label="成绩状态" value="${examGrade.status!}" /]
    [@b.textfield name="examGrade.operator" label="操作者" value="${examGrade.operator!}"/]
    [@b.textfield name="examGrade.percent" label="百分比描述" value="${examGrade.percent!}"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]