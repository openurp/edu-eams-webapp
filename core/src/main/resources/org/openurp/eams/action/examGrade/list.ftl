[#ftl]
[@b.head/]
[@b.grid items=examGrades var="examGrade"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="id" title="id"]${examGrade.id}[/@]

    [@b.col width="20%" property="passed" title="是否通过"]${(examGrade.passed?string("是","否"))!}[/@]

    [@b.col width="15%" property="gradeType" title="成绩类型"]${examGrade.gradeType.name!}[/@]
    [@b.col width="15%" property="markStyle" title="成绩记录方式"]${examGrade.markStyle.name!}[/@]
    [@b.col width="15%" property="examStatus" title="考试情况"]${(examGrade.examStatus.name)!}[/@]
    [@b.col width="15%" property="score" title="得分"]${examGrade.score!}[/@]
  [/@]
  [/@]
[@b.foot/]
