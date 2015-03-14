package org.openurp.edu.eams.teach.grade.interceptor

import java.util.List
import org.apache.commons.collections.CollectionUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.inject.Container
import org.beangle.commons.inject.ContainerAware
import org.beangle.commons.lang.Option
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.system.validate.model.Challenge
import org.openurp.edu.eams.system.validate.service.ChallengeGenerator
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.teach.lesson.Lesson
import com.opensymphony.xwork2.ActionContext
import com.opensymphony.xwork2.ActionInvocation
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor

import scala.collection.JavaConversions._

@SerialVersionUID(806584405241063674L)
class GradeInputMethodInterceptor extends MethodFilterInterceptor with ContainerAware {

  private var baseCodeService: BaseCodeService = _

  private var entityDao: EntityDao = _

  private var courseGradeService: CourseGradeService = _

  private var challengeGenerator: ChallengeGenerator = _

  protected override def doIntercept(invocation: ActionInvocation): String = {
    var lessonId = Params.getLong("lesson.id")
    if (lessonId == null) lessonId = Params.getLong("lessonId")
    if (lessonId == null) return "error"
    val gradeInputSwitch = getGradeInputSwitch(lessonId)
    if (!gradeInputSwitch.isNeedValidate || !gradeInputSwitch.checkOpen()) return invocation.invoke()
    val challenge = ActionContext.getContext.getSession.get(Challenge.SessionAttributeName).asInstanceOf[Challenge]
    if (challenge != null) {
      if (!challenge.isValid) {
        val userCode = Params.get("userresponse")
        challenge.setValid(challengeGenerator.validate(challenge, userCode))
      }
      if (challenge.isValid) return invocation.invoke()
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val state = courseGradeService.getState(lesson)
    if (null != state) {
      ContextHelper.put("gradeState", state)
    }
    if (null != challenge) ContextHelper.put("challenge", challenge)
    ContextHelper.put("lesson", lesson)
    ContextHelper.put("gradeInputSwitch", gradeInputSwitch)
    ContextHelper.put("timeToLiveMinutes", challengeGenerator.getTimeToLiveMinutes)
    ContextHelper.put("servletPath", ServletActionContext.getRequest.getServletPath)
    Challenge.ERROR
  }

  private def getGradeInputSwitch(lessonId: java.lang.Long): GradeInputSwitch = {
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "switch")
    query.where("switch.project=:project", lesson.getProject)
    query.where("switch.semester=:semester", lesson.getSemester)
    query.where("switch.opened = true")
    val rs = entityDao.search(query)
    var gradeInputSwitch: GradeInputSwitch = null
    if (CollectionUtils.isNotEmpty(rs)) {
      gradeInputSwitch = rs.get(0)
    } else {
      gradeInputSwitch = Model.newInstance(classOf[GradeInputSwitch])
      gradeInputSwitch.setProject(lesson.getProject)
      gradeInputSwitch.setSemester(lesson.getSemester)
      gradeInputSwitch.setTypes(CollectUtils.newHashSet(baseCodeService.getCodes(classOf[GradeType])))
    }
    gradeInputSwitch
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setChallengeGenerator(challengeGenerator: ChallengeGenerator) {
    this.challengeGenerator = challengeGenerator
  }

  def setContainer(container: Container) {
    val entityDaoopt = container.getBean("entityDao")
    this.entityDao = entityDaoopt.get
    val baseCodeServiceOpt = container.getBean("baseCodeService")
    this.baseCodeService = baseCodeServiceOpt.get
    val courseGradeServiceOpt = container.getBean("courseGradeService")
    this.courseGradeService = courseGradeServiceOpt.get
    val challengeGeneratorOpt = container.getBean(classOf[ChallengeGenerator])
    this.challengeGenerator = challengeGeneratorOpt.get
  }
}
