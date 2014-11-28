package org.openurp.eams.grade.domain

import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.CourseGrade
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.beangle.webmvc.api.context.Params
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.CourseGrade
import org.beangle.commons.lang.SystemInfo.User
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.CourseGrade
import org.beangle.commons.lang.SystemInfo.User
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.CourseGrade
import org.beangle.commons.lang.SystemInfo.User
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.CourseGrade
import org.beangle.commons.lang.SystemInfo.User
import org.openurp.eams.grade.CourseGradeState
import org.beangle.commons.lang.Strings
import org.openurp.teach.code.GradeType
import java.util.Collection
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.model.CourseGradeStateBean
import scala.collection.mutable.HashSet
import java.util.Arrays

/**
 * 成绩查询管理辅助类
 *
 * @author chaostone
 */
class CourseGradeHelper {

  var entityDao: EntityDao = _

  var baseCodeService: BaseCodeService = _

  var calculator: CourseGradeCalculator = _

  var courseGradeService: CourseGradeService = _

  var gradeRateService: GradeRateService = _

  //	public CourseGradeHelper() {
  //		super()
  //	}

  /**
   * 修改成绩(包括各种成绩成分)
   *
   * @return
   */
  //	def editGrade() ={
  //		var courseGradeId = Params.getInt("courseGradeId").get
  //		if (courseGradeId == null) {
  //			courseGradeId = Params.getInt("courseGrade.id").get
  //		}
  //		val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
  //		ContextHelper.put("courseGrade", courseGrade)
  //		ContextHelper.put("courseTypes", baseCodeService.getCodes(CourseType.class))
  //		ContextHelper.put("examStatuses", baseCodeService.getCodes(ExamStatus.class))
  //		ContextHelper.put("courseTakeTypes", baseCodeService.getCodes(CourseTakeType.class))
  //		ContextHelper.put("markStyles",gradeRateService.getMarkStyles(courseGrade.getProject()))
  //		ContextHelper.put("converter", gradeRateService)
  //
  //		// FIXME examGradeAlterInfos
  //		// ContextHelper.put("courseGradeAlterInfos",
  //		// entityDao.get(CourseGradeAlterInfo.class,
  //		// "grade", courseGrade))
  //		// Map<ExamGrade,List<ExamGradeAlterInfo>> examGradeAlterInfos =
  //		// CollectUtils.newHashMap()
  //		// for (ExamGrade examGrade : courseGrade.getExamGrades()) {
  //		// examGradeAlterInfos.put(examGrade,
  //		// entityDao.get(ExamGradeAlterInfo.class,
  //		// "examGrade",examGrade))
  //		// }
  //		// ContextHelper.put("examGradeAlterInfos", examGradeAlterInfos)
  //		ContextHelper.put("courseGradeAlterInfos", Collections.emptyList())
  //		ContextHelper.put("examGradeAlterInfos", Collections.emptyList())
  //		ContextHelper.put("gradeState", courseGradeService.getState(courseGrade.getLesson()))
  //
  //		// 要添加无成绩的成绩类型
  //
  //		List<GradeType> allTypes = baseCodeService.getCodes(GradeType.class)
  //		for (GradeType type : allTypes) {
  //			if (type.getId().equals(GradeTypeConstants.FINAL_ID)) {
  //				continue
  //			}
  //			if (null == courseGrade.getExamGrade(type)) {
  //				ExamGrade grade = Model.newInstance(ExamGrade.class)
  //				grade.setMarkStyle(courseGrade.getMarkStyle())
  //				grade.setGradeType(type)
  //				grade.setExamStatus(entityDao.get(ExamStatus.class, ExamStatus.NORMAL))
  //				courseGrade.addExamGrade(grade)
  //			}
  //		}
  //	}
  //
  //	/**
  //	 * 保存页面修改的课程成绩
  //	 * 
  //	 * @param user
  //	 */
  //	public void saveGrade(User user) {
  //		Long courseGradeId = Params.getLong("courseGrade.id")
  //		CourseGrade courseGrade = entityDao.get(CourseGrade.class, courseGradeId)
  //		courseGrade.setUpdatedAt(new Date())
  //		courseGrade.setOperator(user.getName())
  //		// 更新课程类别和修读类别
  //		courseGrade
  //		        .setCourseType(entityDao.get(CourseType.class, Params.getInt("courseGrade.courseType.id")))
  //		courseGrade.setCourseTakeType(entityDao.get(CourseTakeType.class,
  //		        Params.getInt("courseGrade.courseTakeType.id")))
  //
  //		courseGrade.setMarkStyle(entityDao.get(ScoreMarkStyle.class,
  //		        Params.getInt("courseGrade.markStyle.id")))
  //
  //		Integer status = Params.getInt("courseGrade.status")
  //		if (null != status) {
  //			courseGrade.setStatus(status)
  //		}
  //
  //		// 更新成绩,是否通过,以及计算总评成绩,并在必要的时候增加成绩修改记录
  //		// 添加用户添加的其他考试成绩
  //		Integer[] gradeTypeIds = Strings.splitToInt(Params.get("gradeTypeId"))
  //		if (null != gradeTypeIds && gradeTypeIds.length != 0) {
  //			for (int i = 0 i < gradeTypeIds.length i++) {
  //				GradeType gradeType = entityDao.get(GradeType.class, gradeTypeIds[i])
  //				ExamGrade examGrade = courseGrade.getExamGrade(gradeType)
  //				Float score = Params.getFloat("score" + gradeTypeIds[i])
  //				// 空成绩忽略掉
  //				Integer examStatusId = Params.getInt("examStatusId" + gradeTypeIds[i])
  //				if (null == examGrade) {
  //					examGrade = Model.newInstance(ExamGrade.class)
  //					examGrade.setGradeType(gradeType)
  //					ScoreMarkStyle examMarkStyle = courseGrade.getMarkStyle()
  //					if (null != courseGrade.getLesson()) {
  //						CourseGradeState courseGradeState = courseGradeService.getState(courseGrade
  //						        .getLesson())
  //						if (null != courseGradeState) {
  //							ExamGradeState examGradeTypeState = courseGradeState.getState(gradeType)
  //							if (null != examGradeTypeState) {
  //								examMarkStyle = examGradeTypeState.getScoreMarkStyle()
  //							}
  //						}
  //					}
  //					examGrade.setMarkStyle(examMarkStyle)
  //				}
  //				// 是否是有效的考试成绩
  //				if (null != score || null != examStatusId && !examStatusId.equals(ExamStatus.NORMAL)) {
  //					Integer examStatus = Params.getInt("status" + gradeTypeIds[i])
  //					if (null == examStatus) {
  //						examStatus = new Integer(Grade.Status.CONFIRMED)
  //					}
  //					examGrade.setStatus(examStatus)
  //					examGrade.setExamStatus(new ExamStatus(examStatusId))
  //					Integer examMarkStyleId = Params.getInt("markStyleId" + gradeTypeIds[i])
  //					if (null != examMarkStyleId) {
  //						examGrade.setMarkStyle(entityDao.get(ScoreMarkStyle.class, examMarkStyleId))
  //					}
  //					if (examGrade.isTransient()) {
  //						examGrade.setScore(score)
  //						courseGrade.addExamGrade(examGrade)
  //					} else {
  //						examGrade.setScore(score)
  //						// updateScore(score, user)
  //					}
  //				} else {
  //					if (examGrade.isPersisted()) {
  //						courseGrade.getExamGrades().remove(examGrade)
  //					}
  //				}
  //			}
  //		}
  //		// 首先计算总评成绩
  //		Boolean updateGrade = Params.getBoolean("updateGrade")
  //		// 再更新最终成绩
  //		if (Boolean.TRUE.equals(updateGrade)) {
  //			Float score = Params.getFloat("courseGrade.score")
  //			calculator.updateScore(courseGrade, score)
  //		} else {
  //			if (null != courseGrade.getLesson()) {
  //				CourseGradeState state = courseGradeService.getState(courseGrade.getLesson())
  //				calculator.calc(courseGrade, state)
  //			} else {
  //				calculator.calc(courseGrade, null)
  //			}
  //		}
  //		entityDao.saveOrUpdate(courseGrade)
  //	}
  //
  /**
   * 删除教学任务某一类型的所有成绩
   *
   * @return
   */
  def removeLessonGrade(userId: Int): String = {
    val lessonId = Params.getInt("lessonId").get
    val lesson = entityDao.get(classOf[Lesson], new Integer(lessonId))

    var state: CourseGradeState = courseGradeService.getState(lesson)
    // 在必要时生成新的成绩状态
    if (null == state) {
      state = new CourseGradeStateBean(lesson)
      entityDao.saveOrUpdate(state)
    }
    val gradeTypeIds = new HashSet[Integer]
    val gradeTypeId = Params.getInt("gradeTypeId") match {
      case Some(gradeTypeId) => gradeTypeIds += gradeTypeId
      case _ => {
        val gradeTypeIdSeq = Params.get("gradeTypeIds")
        if (gradeTypeIdSeq.isDefined) {
          for(id <- Strings.splitToInt(gradeTypeIdSeq.get)){
            gradeTypeIds+=id
          }
        }
      }
    }
    val gradeTypes = entityDao.find(classOf[GradeType], gradeTypeIds)
    for (gradeType <- gradeTypes) {
      if (null != state.getState(gradeType) && state.getState(gradeType).published) { return "error.grade.modifyPublished" }
    }

    if (gradeTypeIds.contains(GradeType.EndGa)) {
      courseGradeService.remove(lesson, entityDao.get(classOf[GradeType], GradeType.EndGa))
    } else {
      for (gradeType <- gradeTypes) {
        courseGradeService.remove(lesson, gradeType)
      }
    }
    return null
  }

  //	/**
  //	 * 删除教学任务几个学生所有成绩
  //	 * 
  //	 * @return
  //	 */
  //	public String removeStdGrade() {
  //		Lesson lesson = entityDao.get(Lesson.class, Params.getLong("lessonId"))
  //		CourseGradeState state = courseGradeService.getState(lesson)
  //		// 在必要时生成新的成绩状态
  //		if (null == state) {
  //			state = new CourseGradeStateBean(lesson)
  //			// FIXME zhouqi 2011-06-07 这里的保存和删除应该同时做，即一起commit
  //			entityDao.saveOrUpdate(state)
  //		}
  //		// 检查成绩状态中可以录入的成绩类型
  //		// 发布的成绩不能修改或删除
  //		if (state.isPublished()) { return "error.grade.modifyPublished" }
  //		Long[] gradeIds = Strings.splitToLong(Params.get("courseGradeIds"))
  //
  //		if (null != gradeIds && gradeIds.length != 0) {
  //			entityDao.remove(entityDao.get(CourseGrade.class, gradeIds))
  //		}
  //		return null
  //	}

}