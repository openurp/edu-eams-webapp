package org.openurp.eams.action.code

import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.edu.base.code.StudyType
import org.openurp.base.code.Degree
import org.openurp.base.code.ProfessionalTitle
import org.openurp.base.code.ProfessionalTitleLevel

class DegreeAction extends RestfulAction[Degree] 
class StudyTypeAction extends RestfulAction[StudyType] 
class TeacherTitleAction extends RestfulAction[ProfessionalTitle] 
class TeacherTitleLevelAction extends RestfulAction[ProfessionalTitleLevel] 
