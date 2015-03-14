package org.openurp.edu.eams.teach.textbook.service

import org.openurp.edu.eams.teach.textbook.TextbookOrderLine

import scala.collection.JavaConversions._

trait TextbookOrderLineCodeGenerator {

  def genCode(orderLine: TextbookOrderLine): String
}
