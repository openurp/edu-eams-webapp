package org.openurp.edu.eams.base.web.action.code

import java.util.ArrayList
import java.util.Collection
import java.util.List
import org.apache.commons.lang3.ClassUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.code.nation.Division
import org.openurp.edu.eams.core.code.ministry.Discipline
import org.openurp.edu.eams.core.code.ministry.DisciplineCatalog
import org.openurp.edu.eams.core.code.ministry.DisciplineCategory
import org.openurp.edu.eams.core.code.nation.TeacherTitleLevel
import org.openurp.edu.base.code.StdLabelType
import org.openurp.edu.eams.core.web.action.biz.Catalog
import org.openurp.edu.eams.core.web.action.biz.Disc

import scala.collection.JavaConversions._

class ManageAction extends AbstractManageAction {

  def editTeacherType(): String = getExtEditForward

  def editCountry(): String = getExtEditForward

  def editGradeLevel(): String = getExtEditForward

  def editCurrencyCategory(): String = getExtEditForward

  def editExamDelayReason(): String = getExtEditForward

  def editHSKDegree(): String = getExtEditForward

  def editPunishmentType(): String = getExtEditForward

  def editAwardType(): String = getExtEditForward

  def editLanguageAbility(): String = getExtEditForward

  def editStudentInfoStatus(): String = getExtEditForward

  def editStdLabel(): String = {
    put("stdLabelTypeList", baseCodeService.getCodes(classOf[StdLabelType]))
    getExtEditForward
  }

  def editTeacherTitle(): String = {
    put("titleLevels", baseCodeService.getCodes(classOf[TeacherTitleLevel]))
    getExtEditForward
  }

  def editDiscipline(): String = {
    val d_catalogs = baseCodeService.getCodes(classOf[DisciplineCatalog])
    val catalogs = new ArrayList[Catalog]()
    for (d_catalog <- d_catalogs) {
      catalogs.add(new Catalog(d_catalog))
    }
    for (catalog <- catalogs) {
      val firstdiscsQry = OqlBuilder.from(classOf[Discipline], "disc")
      firstdiscsQry.where("disc.parent is null").where("disc.catalog.id=:catalogid", catalog.getId)
      val firstdiscs = entityDao.search(firstdiscsQry)
      for (firstdisc <- firstdiscs) {
        catalog.getFirstdiscs.add(new Disc(firstdisc))
      }
    }
    put("catalogs", catalogs)
    put("categories", baseCodeService.getCodes(classOf[DisciplineCategory]))
    getExtEditForward
  }

  def editDivision(): String = {
    try {
      val classNames = get("className1")
      val className = Strings.split(classNames, ",")(0)
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      val divisions = entityDao.getAll(classOf[Division])
      val division = populateEntity(classOf[Division], shortName)
      if (null != division.getId) {
        divisions.remove(division)
      }
      put("division", division)
      put("divisions", divisions)
      EXT + shortName + "Form"
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        throw new RuntimeException(e)
      }
    }
  }

  def editRegisterState(): String = getExtEditForward

  def editUnRegisterReason(): String = getExtEditForward
}
