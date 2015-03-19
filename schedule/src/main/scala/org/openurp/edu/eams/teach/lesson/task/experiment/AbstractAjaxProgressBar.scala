package org.openurp.edu.eams.teach.lesson.task.experiment



import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import org.beangle.commons.lang.tuple.Pair




abstract class AbstractAjaxProgressBar extends AjaxProgressBar {

  
  val id = System.currentTimeMillis()

  
  val messagePool = new LinkedBlockingQueue[String]()

  def finish() {
    messagePool.put(OVER_MSG)
    new Timer().schedule(new DestoryTask(this.id), DESTORY_DELAY)
  }

  private def json(json: Map[String, String]): String = {
    val sb = new StringBuilder()
    sb.append("{")
    for (key <- json.keySet) {
      sb.append(key).append(':').append('\'').append(json.get(key))
        .append('\'')
        .append(',')
    }
    val commaindex = sb.lastIndexOf(",")
    if (commaindex == sb.length - 1) {
      sb.deleteCharAt(sb.length - 1)
    }
    sb.append("}")
    sb.toString
  }

  def notify(kind: Status, message: String, increase: Boolean) {
    val json = new HashMap[String, String]()
    json.put("status", kind.toString)
    json.put("message", message)
    json.put("increase", java.lang.Boolean.toString(increase))
    messagePool.put(json(json))
  }

  def notify(kind: Status, 
      message: String, 
      increase: Boolean, 
      extras: Pair[String, String]*) {
    val json = new HashMap[String, String]()
    json.put("status", kind.toString)
    json.put("message", message)
    json.put("increase", java.lang.Boolean.toString(increase))
    for (Pair <- extras) {
      json.put(Pair.getLeft, Pair.getRight)
    }
    messagePool.put(json(json))
  }
}

class DestoryTask(var barId: java.lang.Long) extends TimerTask {

  override def run() {
    AjaxProgressBarFactory.barPool.remove(barId)
  }
}
