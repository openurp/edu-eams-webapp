package org.openurp.edu.eams.teach.election.service

import java.util.Collection
import java.util.Date
import java.util.List
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

trait ElectLoggerService {

  def genLogger(courseTake: CourseTake, 
      `type`: ElectRuleType, 
      turn: java.lang.Integer, 
      createdAt: Date): ElectLogger

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType, date: Date): List[ElectLogger]

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType, turn: java.lang.Integer): List[ElectLogger]

  def genLogger(courseTakes: Collection[CourseTake], `type`: ElectRuleType): List[ElectLogger]

  def genLogger(courseTake: CourseTake, `type`: ElectRuleType, turn: java.lang.Integer): ElectLogger

  def genLogger(courseTake: CourseTake, `type`: ElectRuleType): ElectLogger
}
