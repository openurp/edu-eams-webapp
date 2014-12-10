[#ftl]
[@b.head/]
[@b.grid items=studentJournals var="studentJournal"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="id" title="ID"]${studentJournal.id}[/@]
    [@b.col width="15%" property="grade" title="年级"]${studentJournal.grade!}[/@]
    [@b.col width="15%" property="major" title="专业"]${studentJournal.major.name!}[/@]
    [@b.col width="10%" property="department" title="行政管理院系"]${studentJournal.department.name!}[/@]
  [/@]
[/@]
[@b.foot/]
