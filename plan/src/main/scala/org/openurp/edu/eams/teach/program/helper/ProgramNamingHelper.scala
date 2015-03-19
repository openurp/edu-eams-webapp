package org.openurp.edu.eams.teach.program.helper

import java.text.MessageFormat
import org.beangle.commons.dao.EntityDao
import com.ekingstar.eams.core.Direction
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.core.Student
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
//remove if not needed


object ProgramNamingHelper {

  private val NAJOR_NAMING_FMT = "{0} {1}"

  private val PERSONAL_NAMING_FMT = "{0}({1})个人计划"

  def name(program: Program): String = {
    MessageFormat.format(NAJOR_NAMING_FMT, program.getMajor.getName, if (program.getDirection == null) "" else program.getDirection.getName)
  }

  def name(program: Program, std: Student): String = name(program)

  def name(std: Student): String = {
    MessageFormat.format(PERSONAL_NAMING_FMT, std.getName, std.getCode)
  }

  def name(genParameter: MajorPlanGenParameter, entityDao: EntityDao): String = {
    val major = entityDao.get(classOf[Major], genParameter.getMajor.id)
    var direction: Direction = null
    if (genParameter.getDirection != null && genParameter.getDirection.id != null) {
      direction = entityDao.get(classOf[Direction], genParameter.getDirection.id)
    }
    MessageFormat.format(NAJOR_NAMING_FMT, genParameter.getGrade, major.getName, if (direction == null) "" else direction.getName)
  }
}
