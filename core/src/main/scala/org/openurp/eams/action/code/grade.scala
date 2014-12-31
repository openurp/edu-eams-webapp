package org.openurp.eams.action.code

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.annotation.code
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.edu.teach.code.ScoreMarkStyle
import org.openurp.edu.teach.code.CourseHourType
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.code.CourseCategory
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.code.CourseAbilityRate




class CourseAbilityRateAction extends RestfulAction[CourseAbilityRate]
class CourseCategoryAction extends RestfulAction[CourseCategory]
class CourseHourTypeAction extends RestfulAction[CourseHourType]
class CourseTakeTypeAction extends RestfulAction[CourseTakeType]

class CourseTypeAction extends RestfulAction[CourseType]{}

class GradeTypeAction extends RestfulAction[GradeType]
class ScoreMarkStyleAction extends RestfulAction[ScoreMarkStyle]

