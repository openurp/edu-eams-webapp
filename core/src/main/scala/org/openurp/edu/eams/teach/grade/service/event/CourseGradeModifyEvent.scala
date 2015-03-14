package org.openurp.edu.eams.teach.grade.service.event

import java.util.List
import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.teach.grade.CourseGrade

@SerialVersionUID(-3680027610530167290L)
class CourseGradeModifyEvent(source: List[CourseGrade]) extends BusinessEvent(source)
