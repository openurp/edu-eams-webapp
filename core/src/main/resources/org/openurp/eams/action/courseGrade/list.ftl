[#ftl]
[@b.head/]
[@b.grid items=courseGrades var="courseGrade"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="id" title="id"]${courseGrade.id}[/@]

    [@b.col width="20%" property="std" title="学生"]${(courseGrade.std.name)!}[/@]

    [@b.col width="15%" property="course" title="课程"]${(courseGrade.course.name)!}[/@]
    [@b.col width="15%" property="courseType" title="课程类别"]${courseGrade.courseType.name!}[/@]
    [#-- 
    [@b.col width="15%" property="semester" title="学期"]${(courseGrade.semester.name)!}[/@]
   --]
    [@b.col width="15%" property="score" title="得分"]${courseGrade.score!}[/@]
  [/@]
  [/@]
[@b.foot/]
