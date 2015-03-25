package org.openurp.edu.eams.core.web.action


import java.util.Arrays

import java.util.Date




import org.apache.commons.beanutils.BeanComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.web.action.BaseInfoAction
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.eams.core.service.AdminclassService



class AdminclassSearchAction extends BaseInfoAction {

  def getEntityName(): String = classOf[Adminclass].getName

  var adminclassService: AdminclassService = _

  def search(): String = {
    if (Strings.isEmpty(getDepartmentIdSeq) || Strings.isEmpty(getStdTypeIdSeq)) {
      return forwardError("对不起，您没有权限！")
    }
    put("adminclasses", entityDao.search(getQueryBuilder))
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[Adminclass] = {
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass")
    populateConditions(builder)
    val vaild = getBoolean("fake.adminclass.valid")
    val now = new Date()
    if (true == vaild) {
      builder.where("adminclass.effectiveAt <= :now and (:now <= adminclass.invalidAt or adminclass.invalidAt is null)", 
        now)
    } else if (false == vaild) {
      builder.where("not(adminclass.effectiveAt <= :now and (:now <= adminclass.invalidAt or adminclass.invalidAt is null))", 
        now)
    }
    if (Strings.isBlank(get(Order.ORDER_STR))) {
      builder.orderBy("adminclass.grade desc")
    } else {
      builder.orderBy(get(Order.ORDER_STR))
    }
    builder.where("adminclass.department in (:departments)", getDeparts)
    if (CollectUtils.isNotEmpty(getStdTypes)) {
      builder.where("adminclass.stdType in (:stdTypes)", getStdTypes)
    }
    if (CollectUtils.isNotEmpty(getEducations)) {
      builder.where("adminclass.education in (:educations)", getEducations)
    }
    builder.limit(getPageLimit)
    builder
  }

  protected def getExportDatas(): Iterable[_] = {
    entityDao.search(getQueryBuilder.limit(null))
  }

  def info(): String = {
    var adminclassId = getInt("adminclass.id")
    if (null == adminclassId) {
      adminclassId = getInt("adminclassId")
    }
    val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
    put("adminclass", adminclass)
    val builder = OqlBuilder.from(classOf[Student], "student")
    builder.where("student.adminclass=:adminClass", adminclass)
    builder.orderBy("code")
    val status = new HashMap[String, StudentJournal]()
    val students = entityDao.search(builder)
    if (CollectUtils.isNotEmpty(students)) {
      for (student <- students) {
        val query = OqlBuilder.from(classOf[StudentJournal], "studentJournal")
          .where("beginOn <= :now and :now <= endOn", new Date())
          .where("studentJournal.std=:student", student)
        val studentJournals = entityDao.search(query)
        if (CollectUtils.isNotEmpty(studentJournals)) {
          status.put(studentJournals.get(0).getStd.getCode, studentJournals.get(0))
        }
      }
    }
    put("students", students)
    put("status", status)
    forward()
  }

  def batchPrint(): String = {
    val ids = Strings.splitToInt(get("adminclassIds"))
    val adminClassList = entityDao.get(classOf[Adminclass], "id", ids)
    val status = new HashMap[String, StudentJournal]()
    if (CollectUtils.isNotEmpty(adminClassList)) {
      for (adminclass <- adminClassList if CollectUtils.isNotEmpty(adminclass.getStudents); student <- adminclass.getStudents) {
        val query = OqlBuilder.from(classOf[StudentJournal], "studentJournal")
          .where("beginOn <= :now and :now <= endOn", new Date())
          .where("studentJournal.std=:student", student)
        val studentJournals = entityDao.search(query)
        if (CollectUtils.isNotEmpty(studentJournals)) {
          status.put(studentJournals.get(0).getStd.getCode, studentJournals.get(0))
        }
      }
    }
    put("adminClasses", adminClassList)
    val stdMap = new HashMap()
    var iter = adminClassList.iterator()
    while (iter.hasNext) {
      val admin = iter.next().asInstanceOf[Adminclass]
      var selectStds: Iterable[_] = null
      selectStds = new ArrayList(admin.getStudents)
      val stds = Array.ofDim[Student](selectStds.size)
      selectStds.toArray(stds)
      Arrays.sort(stds, new BeanComparator("code"))
      stdMap.put(admin.id.toString, stds)
    }
    put("status", status)
    put("stdMap", stdMap)
    forward()
  }

  def maintainAdminclassStd(): String = {
    var adminclassId = getInt("adminclass.id")
    if (null == adminclassId) {
      adminclassId = getInt("adminclassId")
    }
    val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
    put("adminclass", adminclass)
    val builder = OqlBuilder.from(classOf[Student], "student")
    builder.where("student.adminclass=:adminClass", adminclass)
    builder.limit(getPageLimit)
    put("students", entityDao.search(builder))
    forward()
  }
}
