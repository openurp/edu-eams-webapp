package org.openurp.edu.eams.teach.program.majorapply.model.component

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.MajorCourseGroup

//remove if not needed


@SerialVersionUID(2806576960132247993L)
@Embeddable
class FakeCourseGroup extends Serializable {

  @Column(name = "group_id")
  
  var id: java.lang.Long = _

  @JoinColumn(name = "fake_course_type_id")
  @ManyToOne(fetch = FetchType.LAZY)
  
  var courseType: CourseType = _

  def this(group: MajorCourseGroup) {
    this()
    if (group != null) {
      this.id = group.id
      this.courseType = group.getCourseType
    }
  }
}
