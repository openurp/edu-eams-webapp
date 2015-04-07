package org.openurp.edu.eams.teach.schedule.model


import org.openurp.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import org.beangle.data.model.bean.LongIdBean

object CollisionResource {

  object ResourceType extends Enumeration {

    val ADMINCLASS = new ResourceType()

    val CLASSROOM = new ResourceType()

    val TEACHER = new ResourceType()

    val PROGRAM = new ResourceType()

    class ResourceType extends Val

    implicit def convertValue(v: Value): ResourceType = v.asInstanceOf[ResourceType]
  }
}

class CollisionResource extends LongIdBean {
  import CollisionResource._
  var semester: Semester = _

  var lesson: Lesson = _

  var resourceId: String = _

  var resourceType: ResourceType = _

  def this(semester: Semester,
    lesson: Lesson,
    resourceId: String,
    resourceType: ResourceType) {
    this()
    this.semester = semester
    this.lesson = lesson
    this.resourceId = resourceId
    this.resourceType = resourceType
  }
}
