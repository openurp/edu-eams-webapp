package org.openurp.edu.eams.teach.schedule.model

import java.util.Collection
import java.util.Date
import java.util.Set
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(306610647928241805L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.LessonForDepart")
class LessonForDepart extends LongIdObject() {

  @ElementCollection(targetClass = classOf[Long])
  @Column(name = "LESSON_ID", unique = true)
  @CollectionTable(name = "T_LESSON_FOR_D_L_IDS")
  @NotNull
  @BeanProperty
  var lessonIds: Set[Long] = CollectUtils.newHashSet()

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var department: Department = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @BeanProperty
  var beginAt: Date = _

  @BeanProperty
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

  def addLessonIds(lessonIds: Collection[Long]): Boolean = this.lessonIds.addAll(lessonIds)

  def removeLessonId(lessonId: java.lang.Long): Boolean = this.lessonIds.remove(lessonId)

  def removeLessonIds(lessonIds: Collection[Long]): Boolean = this.lessonIds.removeAll(lessonIds)
}
