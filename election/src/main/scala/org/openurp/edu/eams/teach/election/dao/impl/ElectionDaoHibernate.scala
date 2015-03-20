package org.openurp.edu.eams.teach.election.dao.impl

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types


import java.util.Date

import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.dao.Operation
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Throwables
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.Query
import org.hibernate.engine.spi.SessionImplementor
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint
import org.openurp.edu.eams.teach.election.model.exception.ElectCourseLimitCountException
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.helper.ElectLoggerHelper
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass



class ElectionDaoHibernate extends HibernateEntityDao with ElectionDao {

  def updatePitchOn(task: Lesson, stdIds: Iterable[Long], isPitchOn: java.lang.Boolean) {
    if (stdIds.isEmpty) return
    val hql = "update ElectCourseLog set isPitchOn=:isPitchOn where task=:task and std.id in (:stdIds)"
    val query = getSession.createQuery(hql)
    query.setParameter("task", task)
    query.setParameterList("stdIds", stdIds)
    query.setParameter("isPitchOn", isPitchOn)
    query.executeUpdate()
  }

  def updateStdCount(sql: String, lessonId: java.lang.Long): Int = updateStdCount(sql, lessonId, null)

  def updateStdCount(sql: String, lessonId: java.lang.Long, minLimit: java.lang.Integer): Int = {
    val con = getSession.asInstanceOf[SessionImplementor].connection()
    var count = 0
    try {
      val cstmt = con.prepareStatement(sql)
      if (null != lessonId) {
        cstmt.setLong(1, lessonId)
      }
      if (null != minLimit) {
        cstmt.setInt(2, minLimit)
      }
      count = cstmt.executeUpdate()
      con.commit()
      cstmt.close()
    } catch {
      case e: SQLException => {
        logger.error(Throwables.getStackTrace(e))
        try {
          con.rollback()
        } catch {
          case e1: Exception => 
        }
      }
    }
    count
  }

  def removeElection(courseTake: CourseTake, updateStdCount: Boolean): Int = {
    removeElection(courseTake.getLesson, courseTake.getLesson.getSemester, courseTake.getStd.id, null, 
      updateStdCount)
  }

  def removeElection(lesson: Lesson, state: ElectState): Int = {
    removeElection(lesson, state.getProfile(this).getSemester, state.getStd.id, state.getProfile(this).getTurn, 
      !state.isCheckMinLimitCount)
  }

  def removeElection(lesson: Lesson, 
      semester: Semester, 
      stdId: java.lang.Long, 
      turn: java.lang.Integer, 
      updateStdCount: Boolean): Int = {
    try {
      val saveEntities = CollectUtils.newArrayList()
      val courseTakes = get(classOf[CourseTake], Array("lesson.id", "std.id"), lesson.id, stdId)
      val taked = courseTakes.size
      if (taked > 0) {
        buildRemoveEntities(lesson, semester, stdId, turn, saveEntities, courseTakes, updateStdCount)
        val removeEntities = new ArrayList[Any](courseTakes)
        execute(Operation.remove(removeEntities).saveOrUpdate(saveEntities))
        0
      } else {
        -2
      }
    } catch {
      case e: Exception => {
        logger.error("exec function is failed" + "in delete election task:" + 
          lesson.id + 
          " std:" + 
          stdId)
        throw new RuntimeException(e)
      }
    }
  }

  private def buildRemoveEntities(lesson: Lesson, 
      semester: Semester, 
      stdId: java.lang.Long, 
      turn: java.lang.Integer, 
      saveEntities: List[Entity[_]], 
      courseTakes: List[CourseTake], 
      updateCount: Boolean) {
    if (updateCount) {
      val hql = "update " + classOf[Lesson].getName + 
        " set teachClass.stdCount=teachClass.stdCount - 1 where id=?1"
      val update = executeUpdate(hql, lesson.id)
      if (update == 0) {
        val no = lesson.getNo
        val name = lesson.getCourse.getName
        throw new ElectCourseLimitCountException(name + "[" + no + "] 当前不允许退课,请稍后重试")
      }
    }
    saveEntities.add(lesson)
    for (courseTake <- courseTakes) {
      val logger = Model.newInstance(classOf[ElectLogger])
      logger.setLoggerData(courseTake)
      ElectLoggerHelper.setLoggerData(logger)
      logger.setProject(courseTake.getLesson.getProject)
      logger.setTurn(turn)
      logger.setElectionMode(Model.newInstance(classOf[ElectionMode], if (logger.getOperatorCode == logger.getStdCode) ElectionMode.SELF else ElectionMode.ASSIGEND))
      logger.setType(ElectRuleType.WITHDRAW)
      logger.setCreatedAt(new Date())
      logger.setUpdatedAt(logger.getCreatedAt)
      if (updateCount && courseTake.getLimitGroup != null) {
        courseTake.getLimitGroup.setCurCount(courseTake.getLimitGroup.getCurCount - 1)
        saveEntities.add(courseTake.getLimitGroup)
      }
      saveEntities.add(logger)
    }
  }

  def saveElection(courseTake: CourseTake, checkMaxLimit: Boolean): Int = {
    val lesson = courseTake.getLesson
    val std = courseTake.getStd
    saveElection(std, lesson, courseTake.getCourseTakeType, null, checkMaxLimit)
  }

  def saveElection(std: Student, 
      task: Lesson, 
      courseTakeType: CourseTakeType, 
      state: ElectState, 
      checkMaxLimit: Boolean): Int = {
    val teachClass = task.getTeachClass
    if (!checkMaxLimit || teachClass.getStdCount < teachClass.getLimitCount) {
      val saveEntities = CollectUtils.newArrayList()
      buildSaveEntities(saveEntities, task, std, courseTakeType, if (null == state) null else state.getProfile(this).getTurn, 
        !checkMaxLimit)
      saveOrUpdate(saveEntities)
      0
    } else {
      -3
    }
  }

  private def buildSaveEntities(saveEntities: List[Entity[_]], 
      task: Lesson, 
      std: Student, 
      courseTakeType: CourseTakeType, 
      turn: java.lang.Integer, 
      updateStdCount: Boolean) {
    if (updateStdCount) {
      val hql = "update " + classOf[Lesson].getName + 
        "  set teachClass.stdCount= teachClass.stdCount+1 where id=?1"
      val update = executeUpdate(hql, task.id)
      if (update == 0) {
        throw new Exception(task.getCourse.getName + "[" + task.getNo + "] 人数已满")
      }
    }
    saveEntities.add(task)
    val courseTake = Model.newInstance(classOf[CourseTake])
    courseTake.setLesson(task)
    courseTake.setStd(std)
    courseTake.setCourseTakeType(courseTakeType)
    courseTake.setElectionMode(Model.newInstance(classOf[ElectionMode], ElectionMode.SELF))
    val limitGroup = CourseLimitGroupHelper.getMatchCourseLimitGroup(task, std)
    courseTake.setLimitGroup(limitGroup)
    if (updateStdCount && limitGroup != null) {
      limitGroup.setCurCount(limitGroup.getCurCount + 1)
      saveEntities.add(limitGroup)
    }
    saveEntities.add(courseTake)
    val logger = Model.newInstance(classOf[ElectLogger])
    logger.setLoggerData(courseTake)
    ElectLoggerHelper.setLoggerData(logger)
    logger.setProject(courseTake.getLesson.getProject)
    logger.setTurn(turn)
    logger.setElectionMode(Model.newInstance(classOf[ElectionMode], if (logger.getOperatorCode == logger.getStdCode) ElectionMode.SELF else ElectionMode.ASSIGEND))
    logger.setType(ElectRuleType.ELECTION)
    logger.setCreatedAt(new Date())
    logger.setUpdatedAt(logger.getCreatedAt)
    saveEntities.add(logger)
  }

  def removeAllElection(task: Lesson, semesterId: java.lang.Integer, stdId: java.lang.Long): Int = {
    val con = getSession.asInstanceOf[SessionImplementor].connection()
    var rs = 0
    val strProcedure = "{? = call remove_electresult(?,?,?,?,?,?,?,?,?,?)}"
    var cstmt: CallableStatement = null
    try {
      cstmt = con.prepareCall(strProcedure)
      cstmt.registerOutParameter(1, Types.INTEGER)
      cstmt.setLong(2, task.id.longValue())
      cstmt.setLong(3, semesterId.longValue())
      cstmt.setLong(4, 1l)
      cstmt.setString(5, "127.0.0.1")
      cstmt.setLong(6, stdId.longValue())
      cstmt.setFloat(7, task.getCourse.getCredits)
      cstmt.setLong(8, 1)
      cstmt.setLong(9, 1L)
      cstmt.setInt(10, 1)
      cstmt.setBoolean(11, true)
      cstmt.execute()
      rs = cstmt.getInt(1)
      con.commit()
      cstmt.close()
    } catch {
      case e: SQLException => {
        if (null != cstmt) cstmt.close()
        con.rollback()
        throw new RuntimeException(e)
      }
    }
    rs
  }

  def getEntityDao(): EntityDao = this
}
