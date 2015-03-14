package org.openurp.edu.eams.teach.election.service.impl

import java.util.Collection
import java.util.Date
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.model.ElectLoggerBean
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.ElectLoggerService
import org.openurp.edu.eams.teach.election.service.helper.ElectLoggerHelper
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

class ElectLoggerServiceImpl extends ElectLoggerService {

  def genLogger(courseTake: CourseTake, 
      `type`: ElectRuleType, 
      turn: java.lang.Integer, 
      createdAt: Date): ElectLogger = {
    val logger = new ElectLoggerBean()
    logger.setLoggerData(courseTake)
    ElectLoggerHelper.setLoggerData(logger)
    logger.setTurn(turn)
    logger.setElectionMode(Model.newInstance(classOf[ElectionMode], if (logger.getOperatorCode == logger.getStdCode) ElectionMode.SELF else ElectionMode.ASSIGEND))
    logger.setType(`type`)
    logger.setCreatedAt(createdAt)
    logger.setUpdatedAt(logger.getCreatedAt)
    logger
  }

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType, date: Date): List[ElectLogger] = {
    val loggers = CollectUtils.newArrayList()
    for (courseTake <- courseTakes) {
      loggers.add(genLogger(courseTake, `type`, null, date))
    }
    loggers
  }

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType, turn: java.lang.Integer): List[ElectLogger] = {
    val date = new Date()
    val loggers = CollectUtils.newArrayList()
    for (courseTake <- courseTakes) {
      loggers.add(genLogger(courseTake, `type`, turn, date))
    }
    loggers
  }

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType): List[ElectLogger] = {
    val loggers = CollectUtils.newArrayList()
    for (courseTake <- courseTakes) {
      loggers.add(genLogger(courseTake, `type`))
    }
    loggers
  }

  def genLogger(courseTake: CourseTake, `type`: ElectRuleType, turn: java.lang.Integer): ElectLogger = {
    genLogger(courseTake, `type`, turn, new Date())
  }

  def genLogger(courseTake: CourseTake, `type`: ElectRuleType): ElectLogger = {
    genLogger(courseTake, `type`, null, new Date())
  }
}
