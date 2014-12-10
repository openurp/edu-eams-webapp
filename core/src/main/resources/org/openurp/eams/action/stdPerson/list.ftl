[#ftl]
[@b.head/]
[@b.grid items=stdPersons var="stdPerson"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="code" title="人员编码"]${stdPerson.code}[/@]
    [@b.col width="10%" property="name" title="姓名"][@b.a href="!info?id=${stdPerson.id}"]${stdPerson.name}[/@][/@]
    [@b.col width="15%" property="engName" title="英文名"]${stdPerson.engName!}[/@]
    [@b.col width="5%" property="gender" title="性别"]${stdPerson.gender.name!}[/@]
    [@b.col width="10%" property="country" title="国家地区"]${stdPerson.country.name!}[/@]
    [@b.col width="10%" property="nation" title="民族"]${stdPerson.nation.name!}[/@]
  [/@]
[/@]
[@b.foot/]
