package org.openurp.edu.eams.teach.schedule.service


import org.openurp.base.Room



trait BruteForceArrangeService {

  def bruteForceArrange(context: BruteForceArrangeContext, rooms: Iterable[Room]): Unit
}
