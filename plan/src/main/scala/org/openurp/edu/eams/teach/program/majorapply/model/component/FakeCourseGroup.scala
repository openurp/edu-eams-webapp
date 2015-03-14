package org.openurp.edu.eams.teach.program.majorapply.model.component

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(2806576960132247993L)
@Embeddable
class FakeCourseGroup extends Serializable {

  @Column(name = "group_id")
  @BeanProperty
  var id: java.lang.Long = _

  @JoinColumn(name = "fake_course_type_id")
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var courseType: CourseType = _

  def this(group: MajorPlanCourseGroup) {
    this()
    if (group != null) {
      this.id = group.getId
      this.courseType = group.getCourseType
    }
  }
}
