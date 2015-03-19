package org.openurp.edu.eams.teach.grade.course.web.action


import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.course.model.GradeViewScope
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class GradeViewScopeAction extends RestrictionSupportAction {

  def search(): String = {
    val query = OqlBuilder.from(classOf[GradeViewScope], "scope")
    populateConditions(query)
    val hql = new StringBuilder()
    hql.append("(")
    hql.append("select count(*) ")
    hql.append("from scope.stdTypes ss")
    hql.append(")")
    hql.append(" <= 0")
    hql.append(" or ")
    hql.append("exists (")
    hql.append("select count(*) ")
    hql.append("from scope.stdTypes ss ")
    hql.append("where ss in (:stdTypes)")
    hql.append(")")
    query.where(hql.toString, getStdTypes)
    query.limit(getPageLimit)
    query.orderBy(Order.parse(get("orderBy")))
    query.select("distinct scope")
    put("scopes", entityDao.search(query))
    forward()
  }

  def edit(): String = {
    val scopeId = getLong("scopeId")
    if (null != scopeId) {
      put("scope", entityDao.get(classOf[GradeViewScope], scopeId))
    }
    val projects = getProjects
    val departments = getDeparts
    put("projects", projects)
    val query = OqlBuilder.from(classOf[Student], "student")
    if (CollectUtils.isEmpty(projects) || CollectUtils.isEmpty(departments)) {
      query.where("student is null")
    } else {
      query.where("student.project in (:projects)", getStdTypes)
      query.where("student.department in (:departments)", getDeparts)
    }
    query.select("select distinct student.grade")
    put("enrollYears", entityDao.search(query))
    forward()
  }

  def save(): String = {
    val scope = populateEntity(classOf[GradeViewScope], "scope")
    scope.clearProjects()
    val projectIdSeq = get("projectIds")
    if (Strings.isNotBlank(projectIdSeq)) {
      scope.addProjects(Strings.splitToInt(projectIdSeq))
    }
    scope.clearEducations()
    val educationIdSeq = get("educationIds")
    if (Strings.isNotBlank(educationIdSeq)) {
      scope.addEducations(Strings.splitToInt(educationIdSeq))
    }
    scope.clearStdTypes()
    val stdTypeIdSeq = get("stdTypeIds")
    if (Strings.isNotBlank(stdTypeIdSeq)) {
      scope.addStdTypes(Strings.splitToInt(stdTypeIdSeq))
    }
    entityDao.saveOrUpdate(scope)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    entityDao.remove(entityDao.get(classOf[GradeViewScope], Strings.splitToLong(get("scopeIds"))))
    redirect("search", "info.action.success")
  }
}
