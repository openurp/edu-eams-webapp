package org.openurp.edu.eams.teach.textbook.service

import org.openurp.edu.eams.teach.textbook.TextbookOrderLine



trait TextbookOrderLineCodeGenerator {

  def genCode(orderLine: TextbookOrderLine): String
}
