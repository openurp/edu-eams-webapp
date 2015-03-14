package org.openurp.edu.eams.teach.schedule.service

import java.util.Collection
import org.openurp.base.Room

import scala.collection.JavaConversions._

trait BruteForceArrangeService {

  def bruteForceArrange(context: BruteForceArrangeContext, rooms: Collection[Classroom]): Unit
}
