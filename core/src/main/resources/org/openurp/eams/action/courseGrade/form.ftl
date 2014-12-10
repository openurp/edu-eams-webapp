[#ftl]
[@b.head/]
[@b.toolbar title="修改学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!update?id=${courseGrade.id}" theme="list"]
    [@b.radios label="是否通过"  name="courseGrade.passed" value=courseGrade.passed items="1:common.yes,0:common.no"/]
    [@b.radios label="是否提交"  name="courseGrade.beyondSubmit" value=courseGrade.beyondSubmit items="1:common.yes,0:common.no"/]
    [@b.select name="courseGrade.gradeType.id" label="成绩类型" value="${(courseGrade.gradeType.id)!}" required="true" 
               style="width:200px;" items=gradeTypes option="id,name" empty="..."/]
    [@b.select name="courseGrade.markStyle.id" label="成绩记录方式" value="${(courseGrade.markStyle.id)!}" required="true" 
               style="width:200px;" items=markStyles option="id,name" empty="..."/]
    [@b.select name="courseGrade.examStatus.id" label="考试情况" value="${(courseGrade.examStatus.id)!}" required="true" 
               style="width:200px;" items=examStatuses option="id,name" empty="..."/]
    [@b.select name="courseGrade.courseGrade.id" label="对应的课程成绩" value="${(courseGrade.courseGrade.id)!}" required="true" 
               style="width:200px;" items=courseGrades option="id,name" empty="..."/]

    [@b.textfield name="courseGrade.score" label="得分" value="${courseGrade.score!}" maxlength="30"/]
    [@b.textfield name="courseGrade.scoreText" label="得分字面值" value="${courseGrade.scoreText!}" /]
    [@b.textfield name="courseGrade.status" label="成绩状态" value="${courseGrade.status!}" /]
    [@b.textfield name="courseGrade.operator" label="操作者" value="${courseGrade.operator!}"/]
    [@b.textfield name="courseGrade.percent" label="百分比描述" value="${courseGrade.percent!}"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]