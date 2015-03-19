package org.openurp.edu.eams.teach.program.major.service





@SerialVersionUID(-7714890381891386802L)
class MajorPlanDuplicatedException extends Exception {

  
  var name: String = "该生匹配多个专业培养计划，不能进行相关操作"

  
  var engName: String = "There are more than one Major Plans matched with this Student's Personal Plan"
}
