package org.openurp.eams.action.code

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.annotation.code
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.teach.code.ExamMode
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.ExamType


class ExamModeAction extends RestfulAction[ExamMode]

class ExamStatusAction extends RestfulAction[ExamStatus] 



class ExamTypeAction extends RestfulAction[ExamType] 