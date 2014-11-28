package org.openurp.eams.grade.teacher.util

import org.openurp.teach.grade.CourseGrade
import org.beangle.commons.lang.Objects
import scala.collection.mutable.ListBuffer


/**
 * 统计段<br>
 * 排序按照降序进行 [min,max]
 * 
 * @author chaostone
 */

object FloatSegment {
  

  /**
   * 构造分段区[start,start+span-1],[start+span,start+2*span-1]..
   * 
   * @param start
   * @param span
   * @param count
   * @return
   */
  def  buildSegments(_start:Int, span:Int, count:Int):List[FloatSegment]= {
    var start = _start
    val segmentList = new ListBuffer[FloatSegment]()
    for (i <- 0 until count) {
      segmentList.append(new FloatSegment(start, start + span - 1))
      start += span
    }
     segmentList.asInstanceOf[List[FloatSegment]]
  }

  def countSegments( segs:List[FloatSegment],  numbers:List[Number])= {
    for ( number <- numbers) {
      if (null != number){
    	  var added = false
    	  for (element <- segs ) {
    		 if (!added && element.add(number.floatValue())){
    		   added = true
    		 }
    	  }
      }
    }
  }
}

class FloatSegment extends Comparable[Object] {

 var min:Float= 0F

 var max:Float= 0F

 var count:Int= 0

 def this(min:Float, max:Float) {
	 this()
    this.min = min
    this.max = max
    count = 0
  }

  def  add(score:Float):Boolean = {
    if (score <= max && score >= min) {
      count=count+1
      return true
    } 
    else {
      return false
    }
  }

 

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
 def  compareTo(obj:Object):Int = {
    val myClass: FloatSegment = obj.asInstanceOf[FloatSegment]
    java.lang.Float.compare(myClass.min, this.min)
  }

  override def clone():Object = {
    new FloatSegment(min, max)
  }

 def emptySeg():Boolean = {
    if (min == 0 && max == 0) return true
    else return false
  }

  /**
   * @see java.lang.Object#toString()
   */
  override def toString():String = {
    Objects.toStringBuilder(this.getClass()).add("min", this.min).add("max", this.max)
        .add("count", this.count).toString()
  }
}