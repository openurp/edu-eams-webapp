[#ftl]
[@b.head/]
[@b.toolbar title="新建学位"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!save" theme="list"]
    [@b.select name="courseGrade.course.id" label="课程" value="${(courseGrade.course.id)!}" required="true" 
               style="width:200px;" items=courses option="id,name" empty="..."/]
    [@b.select name="courseGrade.courseTakeType.id" label="修读类别" value="${(courseGrade.courseTakeType.id)!}" required="true" 
               style="width:200px;" items=courseTakeTypes option="id,name" empty="..."/]
    [@b.select name="courseGrade.courseType.id" label="课程类别" value="${(courseGrade.courseType.id)!}" required="true" 
               style="width:200px;" items=courseTypes option="id,name" empty="..."/]
    [@b.select name="courseGrade.examMode.id" label="考核方式" value="${(courseGrade.examMode.id)!}" required="true" 
               style="width:200px;" items=examModes option="id,name" empty="..."/]
    [@b.select name="courseGrade.semester.id" label="学期" value="${(courseGrade.semester.id)!}" required="true" 
               style="width:200px;" items=semesters option="id,name" empty="..."/]
    [@b.select name="courseGrade.std.id" label="学生" value="${(courseGrade.std.id)!}" required="true" 
               style="width:200px;" items=stds option="id,name" empty="..."/]
    [@b.select name="courseGrade.markStyle.id" label="成绩记录方式" value="${(courseGrade.markStyle.id)!}" required="true" 
               style="width:200px;" items=markStyles option="id,name" empty="..."/]
    [@b.textfield name="courseGrade.lessonNo" label="课程序号" value="${courseGrade.lessonNo!}" maxlength="30"/]
    [@b.textfield name="courseGrade.gp" label="绩点" value="${courseGrade.gp!}" /]
    [@b.textfield name="courseGrade.remark" label="备注" value="${courseGrade.remark!}" /]
    [@b.radios label="是否通过"  name="courseGrade.passed" value=courseGrade.passed items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="是否提交"  name="courseGrade.beyondSubmit" value=courseGrade.beyondSubmit items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="是否发布"  name="courseGrade.published" value=courseGrade.published items="1:common.yes,0:common.no" required="true"/]
    [@b.radios label="个人百分比"  name="courseGrade.personPercent" value=courseGrade.personPercent items="1:common.yes,0:common.no" required="true"/]
    [@b.textfield name="courseGrade.operator" label="操作者" value="${courseGrade.operator!}"/]
    [@b.textfield name="courseGrade.score" label="得分" value="${courseGrade.score!}"/]
    [@b.textfield name="courseGrade.scoreText" label="得分字面值" value="${courseGrade.scoreText!}"/]
    [@b.textfield name="courseGrade.status" label="成绩状态" value="${courseGrade.status!}"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]