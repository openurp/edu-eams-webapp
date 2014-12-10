package org.openurp.eams.action.code

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.annotation.code
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.teach.code.CourseAbilityRate
import org.openurp.teach.code.CourseCategory
import org.openurp.teach.code.CourseHourType
import org.openurp.teach.code.CourseTakeType
import org.openurp.teach.code.CourseType
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.ScoreMarkStyle




class CourseAbilityRateAction extends RestfulAction[CourseAbilityRate]
class CourseCategoryAction extends RestfulAction[CourseCategory]
class CourseHourTypeAction extends RestfulAction[CourseHourType]
class CourseTakeTypeAction extends RestfulAction[CourseTakeType]

class CourseTypeAction extends RestfulAction[CourseType]{}

class GradeTypeAction extends RestfulAction[GradeType]
class ScoreMarkStyleAction extends RestfulAction[ScoreMarkStyle]

