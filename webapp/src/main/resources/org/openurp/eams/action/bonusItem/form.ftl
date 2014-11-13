[#ftl]
[@b.head/]
[@b.toolbar title="修改加分项"]bar.addBack();[/@]
[@b.tabs]
  [@b.form action="!update?id=${bonusItem.id}" theme="list"]
    [@b.textfield name="bonusItem.code" label="代码" value="${bonusItem.code!}" required="true" maxlength="22"/]
    [@b.textfield name="bonusItem.name" label="名称" value="${bonusItem.name!}" required="true" maxlength="222"/]
    [@b.textfield name="bonusItem.grade" label="年级" value="${bonusItem.grade!}" required="true"/]
    [@b.textfield name="bonusItem.maxScore" label="最大加分值" value="${bonusItem.maxScore!}" required="true"/]
    [@b.select name="bonusItem.stdLabel.id" label="学生标签" value="${(bonusItem.stdLabel.id)!}" required="true" 
               style="width:200px;" items=stdLabels option="id,name" empty="..."/]
    [@b.select name="bonusItem.beginTime.id" label="开始学期" value="${(bonusItem.beginTime.id)!}" required="true" 
               style="width:200px;" items=beginTimes option="id,name" empty="..."/]
    [@b.select name="bonusItem.endTime.id" label="结束学期" value="${(bonusItem.endTime.id)!}"
               style="width:200px;" items=endTimes option="id,name" empty="..."/]
    [@b.select2 label="加分课程列表" name1st="coursesId1st" name2nd="coursesId2nd"  required="true" 
      items1st=courses items2nd= bonusItem.courses option="id,name"/]
    [@b.formfoot]
      [@b.reset/]&nbsp;&nbsp;[@b.submit value="action.submit"/]
    [/@]
  [/@]
[/@]
[@b.foot/]