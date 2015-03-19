package org.openurp.edu.eams.teach.program.personal.service

import com.ekingstar.eams.core.Student
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
//remove if not needed


trait PersonalPlanService {

  def genPersonalPlan(std: Student, majorProgram: Program): PersonalPlan

  def genPersonalPlan(std: Student): PersonalPlan

  def getMajorPlanForDiff(std: Student): MajorPlan
}
