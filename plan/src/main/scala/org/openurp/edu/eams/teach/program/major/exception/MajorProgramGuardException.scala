package org.openurp.edu.eams.teach.program.major.exception

import com.ekingstar.eams.exception.EamsException
//remove if not needed
import scala.collection.JavaConversions._

class MajorProgramGuardException(i18nKey: String) extends EamsException(i18nKey)
