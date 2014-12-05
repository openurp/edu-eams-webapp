package org.openurp.eams.grade.service

import org.openurp.teach.grade.domain.GradeCourseTypeProvider
import org.openurp.teach.code.CourseType
import org.openurp.teach.core.Student
import org.openurp.teach.core.Course

class SimpleGradeCourseTypeProviderImpl extends GradeCourseTypeProvider{

   def getCourseType(std: Student, course: Course, defaultCourseType: CourseType): CourseType={
     defaultCourseType
   }
}