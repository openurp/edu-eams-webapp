package org.openurp.edu.eams.teach.workload.service

import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.code.industry.TeacherType
import org.openurp.edu.eams.core.code.nation.TeacherTitleLevel

import scala.collection.JavaConversions._

trait TeacherPeriodLimitService {

  def getMaxPeriod(teacher: Teacher): Int

  def getMaxPeriod(level: TeacherTitleLevel, `type`: TeacherType): Int

  def getTeacherPeriods(teacher: Teacher, semester: Semester): Int
}
