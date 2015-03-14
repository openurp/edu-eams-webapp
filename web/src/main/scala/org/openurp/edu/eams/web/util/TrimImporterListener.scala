package org.openurp.edu.eams.web.util

import java.util.Iterator
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.importer.listener.ItemImporterListener

import scala.collection.JavaConversions._

class TrimImporterListener extends ItemImporterListener {

  override def onItemStart(tr: TransferResult) {
    var iter = importer.getCurData.keySet.iterator()
    while (iter.hasNext) {
      val key = iter.next().asInstanceOf[String]
      val value = importer.getCurData.get(key)
      if (null != value && value.isInstanceOf[String]) {
        importer.changeCurValue(key, Strings.trim(value.asInstanceOf[String]))
      }
    }
  }
}
