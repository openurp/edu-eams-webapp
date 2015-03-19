package org.openurp.edu.eams.teach.textbook.service.internal

import java.util.UUID
import org.openurp.edu.eams.teach.textbook.TextbookOrderLine
import org.openurp.edu.eams.teach.textbook.service.TextbookOrderLineCodeGenerator



class DefaultTextbookOrderLineCodeGenerator extends TextbookOrderLineCodeGenerator {

  def genCode(orderLine: TextbookOrderLine): String = {
    UUID.randomUUID().toString.substring(0, 16)
  }
}
