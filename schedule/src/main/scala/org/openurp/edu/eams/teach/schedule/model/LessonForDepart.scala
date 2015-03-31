package org.openurp.edu.eams.teach.schedule.model


import java.util.Date








import org.beangle.commons.collection.Collections
import org.beangle.data.model.bean.LongIdBean

import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project




@SerialVersionUID(306610647928241805L)

class LessonForDepart extends LongIdBean() {

  @ElementCollection(targetClass = classOf[Long])
  
  @CollectionTable(name = "T_LESSON_FOR_D_L_IDS")
  
  
  var lessonIds: Set[Long] = Collections.newSet[Any]

  
  
  
  var department: Department = _

  
  
  
  var semester: Semester = _

  
  
  
  var project: Project = _

  
  var beginAt: Date = _

  
  var endAt: Date = _

  def this(lessonIds: Set[Long], 
      department: Department, 
      semester: Semester, 
      project: Project) {
    super()
    this.lessonIds = lessonIds
    this.department = department
    this.semester = semester
    this.project = project
  }

  def addLessonId(lessonId: java.lang.Long): Boolean = this.lessonIds.add(lessonId)

  def addLessonIds(lessonIds: Iterable[Long]): Boolean = this.lessonIds.addAll(lessonIds)

  def removeLessonId(lessonId: java.lang.Long): Boolean = this.lessonIds.remove(lessonId)

  def removeLessonIds(lessonIds: Iterable[Long]): Boolean = this.lessonIds.removeAll(lessonIds)
}
