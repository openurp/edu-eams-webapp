package org.openurp.edu.eams.teach.program.bind.service.impl

import java.util.Date


import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.Student
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.model.StudentProgramBean
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.BindWrongMajorProgramException
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
import org.openurp.edu.eams.teach.program.service.StudentProgramBindService
//remove if not needed


class StudentProgramBindServiceImpl extends BaseServiceImpl with StudentProgramBindService {

  def guessMajorPrograms(student: Student, withStdType: Boolean, withDirection: Boolean): List[Program] = {
    val query = OqlBuilder.from(classOf[Program], "program")
    query.where("program.grade = :grade", student.getGrade)
      .where("program.education = :education", student.getEducation)
      .where("program.department = :department", student.getDepartment)
      .where("program.major = :major", student.getMajor)
      .where("program.auditState = :as", CommonAuditState.ACCEPTED)
    if (withStdType) {
      query.where("program.stdType = :stdType", student.getType)
    }
    if (withDirection) {
      if (null == student.getDirection) {
        query.where("program.direction is null")
      } else {
        query.where("program.direction = :direction", student.getDirection)
      }
    }
    entityDao.search(query)
  }

  def matchMajorProgram(student: Student, withStdType: Boolean, withDirection: Boolean): Program = {
    val programs = guessMajorPrograms(student, withStdType, withDirection)
    if (programs.isEmpty) {
      throw new NoMajorProgramException()
    }
    if (programs.size > 1) {
      throw new AmbiguousMajorProgramException(programs)
    }
    programs.get(0)
  }

  def autobind(student: Student, withStdType: Boolean, withDirection: Boolean) {
    val program = matchMajorProgram(student, withStdType, withDirection)
    val it = entityDao.get(classOf[StudentProgram], "std", student)
      .iterator()
    var sp: StudentProgram = null
    if (it.hasNext) {
      sp = it.next()
    } else {
      sp = Model.newInstance(classOf[StudentProgram])
      sp.setStd(student)
      sp.setCreatedAt(new Date())
    }
    sp.setProgram(program)
    sp.setUpdatedAt(new Date())
    entityDao.saveOrUpdate(sp)
  }

  @Deprecated
  def bind(student: Student, program: Program) {
  }

  def forcebind(student: Student, program: Program) {
    val query = OqlBuilder.from(classOf[StudentProgramBean], "sp")
    query.where("sp.std=:std", student)
    var sp = entityDao.uniqueResult(query)
    if (null == sp) {
      sp = new StudentProgramBean()
      sp.setStd(student)
      sp.setCreatedAt(new Date())
      sp.setUpdatedAt(new Date())
    }
    sp.setProgram(program)
    entityDao.saveOrUpdate(sp)
  }

  def unbind(student: Student) {
    val query = OqlBuilder.from(classOf[StudentProgramBean], "sp")
    query.where("sp.std=:std", student)
    val sp = entityDao.uniqueResult(query)
    if (null != sp) {
      entityDao.remove(sp)
    }
  }
}
