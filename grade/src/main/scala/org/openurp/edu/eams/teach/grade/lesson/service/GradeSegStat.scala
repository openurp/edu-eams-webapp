package org.openurp.edu.eams.teach.grade.lesson.service


import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.util.stat.FloatSegment




class GradeSegStat( var gradeType: GradeType, scoreSegments: List[FloatSegment], grades: List[Grade])
    {

  
  var stdCount: Int = grades.size

  
  var scoreSegments: List[FloatSegment] = Collections.newBuffer[Any]

  
  var heighest: java.lang.Float = grades.get(0).asInstanceOf[Grade].getScore

  
  var lowest: java.lang.Float = grades.get(grades.size - 1).asInstanceOf[Grade].getScore

  
  var average: java.lang.Float = _

  var iter = scoreSegments.iterator()
  while (iter.hasNext) {
    val ss = iter.next()
    this.scoreSegments.add(ss.clone().asInstanceOf[FloatSegment])
  }

  Collections.sort(grades)

  var sum = 0

  var iter = grades.iterator()
  while (iter.hasNext) {
    val grade = iter.next()
    var score = grade.getScore
    if (null == score) {
      score = new java.lang.Float(0)
    }
    sum += score.doubleValue()
    var iterator = this.scoreSegments.iterator()
    while (iterator.hasNext) {
      val scoreSeg = iterator.next()
      if (scoreSeg.add(score)) {
        //break
      }
    }
  }

  if (0 != this.stdCount) {
    this.average = new java.lang.Float(sum / this.stdCount)
  }
}
