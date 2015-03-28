package org.openurp.edu.eams.core.web.action

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat

import java.util.Calendar
import java.util.Date





import java.util.TreeMap
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.time.DateUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.TransferListener
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.excel.ExcelItemReader
import org.beangle.commons.transfer.excel.ExcelTemplateWriter
import org.beangle.commons.transfer.exporter.Context
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.beangle.commons.transfer.exporter.Exporter
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.commons.transfer.exporter.TemplateExporter
import org.beangle.commons.transfer.exporter.TemplateWriter
import org.beangle.commons.transfer.importer.EntityImporter
import org.beangle.commons.transfer.importer.MultiEntityImporter
import org.beangle.commons.transfer.importer.listener.ImporterForeignerListener
import org.beangle.commons.transfer.io.TransferFormat
import org.beangle.commons.web.util.RequestUtils
import org.beangle.ems.dictionary.service.CodeFixture
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.eams.core.model.AdminclassBean
import org.openurp.edu.eams.core.service.listener.AdminclassImportListener
import com.google.gson.Gson
import com.opensymphony.xwork2.ActionContext



class AdminclassAction extends AdminclassSearchAction {

  var importerListeners: List[_ <: TransferListener] = Collections.newBuffer[Any]

  def edit(): String = {
    put("departments", getDeparts)
    put("educations", getEducations)
    put("stdTypes", getStdTypes)
    put("majors", baseInfoService.getBaseInfos(classOf[Major]))
    put("directions", baseInfoService.getBaseInfos(classOf[Direction]))
    if (getIntId("adminclass") == null) {
      put("adminclass", new AdminclassBean())
      return forward()
    }
    put("adminclass", entityDao.get(classOf[Adminclass], getIntId("adminclass")))
    forward()
  }

  def save(): String = {
    val adminclass = populateEntity(classOf[Adminclass], "adminclass")
    if (adminclass.isTransient) {
      adminclass.setCreatedAt(new Date())
    }
    val cal = Calendar.getInstance
    cal.setTime(adminclass.getEffectiveAt)
    adminclass.setEffectiveAt(DateUtils.truncate(adminclass.getEffectiveAt, Calendar.DAY_OF_MONTH))
    if (adminclass.getInvalidAt != null) {
      adminclass.setInvalidAt(DateUtils.truncate(adminclass.getInvalidAt, Calendar.DAY_OF_MONTH))
    }
    adminclass.setUpdatedAt(new Date())
    entityDao.saveOrUpdate(adminclass)
    redirect("search", "info.action.success")
  }

  def checkCode() {
    val newCode = get("newCode")
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass")
    builder.where("adminclass.code=:newCode", newCode)
    val adminclassList = entityDao.search(builder)
    if (adminclassList.size == 0) {
      getResponse.getWriter.append("0")
    } else {
      getResponse.getWriter.append("1")
    }
    getResponse.getWriter.flush()
    getResponse.getWriter.close()
  }

  def checkName() {
    val newName = get("newName")
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass")
    builder.where("adminclass.name=:newName", newName)
    val adminclassList = entityDao.search(builder)
    if (adminclassList.size == 0) {
      getResponse.getWriter.append("0")
    } else {
      getResponse.getWriter.append("1")
    }
    getResponse.getWriter.flush()
    getResponse.getWriter.close()
  }

  private def saveOrUpdate(adminClass: Adminclass): String = {
    if (!codeGenerator.isValidCode(adminClass.getCode)) {
      val code = codeGenerator.gen(new CodeFixture(adminClass))
      if (codeGenerator.isValidCode(code)) {
        adminClass.setCode(code)
      } else {
        addMessage(getText("system.codeGen.failure"))
        return forward(new Action(this.getClass, "edit"))
      }
    }
    adminClass.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()))
    if (!adminClass.isPersisted) {
      adminClass.setCreatedAt(new java.sql.Date(System.currentTimeMillis()))
    }
    onSave(adminClass)
    null
  }

  def batchUpdateStdCount(): String = {
    val adminclassIds = Strings.splitToInt(get("adminclassIds"))
    val adminclassList = entityDao.get(classOf[Adminclass], adminclassIds)
    for (adminclass <- adminclassList) {
      adminclass.setStdCount(adminclass.getStudents.size)
      entityDao.saveOrUpdate(adminclass)
    }
    redirect("search", "info.update.success")
  }

  def removeAdminclass(): String = {
    val adminclassList = entityDao.get(classOf[Adminclass], getIntIds("adminclass"))
    for (adminclass <- adminclassList if adminclass.getStudents.size > 0) {
      return redirect("search", "你选择了有学生的行政班，不能删除这样的行政班")
    }
    entityDao.remove(adminclassList)
    redirect("search", "info.delete.success")
  }

  def downloadAdminclassStdTemp(): String = {
    val context = new Context()
    context.put("format", TransferFormat.Xls)
    val exporter = new TemplateExporter()
    val response = ServletActionContext.getResponse
    val templateWriter = new ExcelTemplateWriter()
    if ("std" == get("templateType")) {
      templateWriter.setTemplate(getResource("template/excel/班级学生导入数据模版.xls"))
    }
    if ("adminclass" == get("templateType")) {
      templateWriter.setTemplate(getResource("template/excel/班级导入数据模版.xls"))
    }
    templateWriter.setOutputStream(response.getOutputStream)
    templateWriter.setContext(context)
    exporter.setWriter(templateWriter)
    response.setContentType("application/vnd.ms-excel;charset=GBK")
    var oldFileName = ""
    if ("std" == get("templateType")) {
      oldFileName = "班级学生导入数据模版.xls"
    }
    if ("adminclass" == get("templateType")) {
      oldFileName = "班级导入数据模版.xls"
    }
    val fileName = RequestUtils.encodeAttachName(ServletActionContext.getRequest, oldFileName)
    response.setHeader("Content-Disposition", "attachment;filename=" + fileName)
    exporter.setContext(context)
    exporter.transfer(new TransferResult())
    null
  }

  protected override def getExportDatas(): Iterable[_] = {
    if ("std" == get("exportType")) {
      val ids = getIntIds("adminclass")
      val builder = OqlBuilder.from(classOf[Student], "student")
      builder.orderBy("student.adminclass.name").orderBy("student.code")
        .limit(null)
      if (Arrays.isEmpty(ids)) {
        val classQuery = OqlBuilder.from(classOf[Adminclass], "adminclass")
        populateConditions(classQuery)
        val classes = entityDao.search(classQuery)
        if (Collections.isEmpty(classes)) {
          return Collections.emptyList()
        } else {
          builder.where("student.adminclass in (:adminclasses)", classes)
        }
      } else {
        builder.where("student.adminclass.id in (:adminclasses)", ids)
      }
      entityDao.search(builder)
    } else {
      val ids = Strings.splitToInt(get("adminclassIds"))
      if (Arrays.isEmpty(ids)) {
        entityDao.search(getQueryBuilder.limit(null))
      } else {
        entityDao.get(classOf[Adminclass], ids)
      }
    }
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    if ("std" == get("exportType")) {
      val adminclassPropertyExtractor = new AdminclassPropertyExtractor(getTextResource)
      val query = OqlBuilder.from(classOf[StudentJournal], "studentJournal")
        .where("beginOn <= :now and :now <= endOn", new Date())
      val ids = ids("adminclass", classOf[Long])
      val builder = OqlBuilder.from(classOf[Student], "student")
      builder.orderBy("student.adminclass.name").orderBy("student.code")
        .limit(null)
      if (Arrays.isEmpty(ids)) {
        val classQuery = getQueryBuilder.limit(null).asInstanceOf[OqlBuilder[Adminclass]]
        populateConditions(classQuery)
        val classes = entityDao.search(classQuery)
        if (Collections.isEmpty(classes)) {
          query.where("1=2")
        } else {
          query.where("studentJournal.std.adminclass in (:adminclasses)", classes)
        }
      } else {
        query.where("studentJournal.std.adminclass.id in (:adminclasses)", getIntIds("adminclass"))
      }
      val journals = entityDao.search(query)
      adminclassPropertyExtractor.setJournals(journals)
      adminclassPropertyExtractor
    } else {
      super.getPropertyExtractor
    }
  }

  protected def onSave(entity: Entity) {
    adminclassService.saveOrUpdate(entity.asInstanceOf[Adminclass])
  }

  def genderStatistic(): String = {
    val id = get("ids").split(",")
    val deptMap = new TreeMap[String, Map[_,_]]()
    var majorMap: Map[String, Map[_,_]] = null
    var fieldMap: Map[String, ArrayList[Adminclass]] = null
    var list: ArrayList[Adminclass] = null
    val mwnum = new HashMap()
    val gender = Array.ofDim[Integer](id.length, 2)
    val major_field = Model.newInstance(classOf[Direction]).asInstanceOf[Direction]
    major_field.setCode("-1")
    major_field.setName(" ")
    for (i <- 0 until id.length) {
      val idl = java.lang.Integer.parseInt(id(i))
      val ac = entityDao.get(classOf[Adminclass], idl).asInstanceOf[Adminclass]
      val s = ac.getStudents
      gender(i)(0) = 0
      gender(i)(1) = 0
      for (obj <- s) {
        val student = obj.asInstanceOf[Student]
        val journals = student.getJournals
        val date = new Date()
        var isInSchool = false
        var iterator = journals.iterator()
        while (iterator.hasNext) {
          val journal = iterator.next()
          if (date.before(journal.getEndOn) && date.after(journal.getBeginOn)) {
            isInSchool = journal.isInschool
          }
        }
        if (student.getGender.getName == "男" && isInSchool) {
          gender(i)(0) += 1
        } else if (student.getGender.getName == "女" && isInSchool) {
          gender(i)(1) += 1
        }
      }
      mwnum.put(ac.getCode, gender(i))
      if (ac.department != null && ac.major != null) {
        val departmentcode = ac.department.getCode
        val majorcode = ac.major.getCode
        var majorfield = ac.direction
        if (deptMap.get(departmentcode) == null) {
          majorMap = new TreeMap[String, Map[_,_]]()
          fieldMap = new TreeMap[String, ArrayList[Adminclass]]()
          list = new ArrayList[Adminclass]()
          list.add(ac)
          if (majorfield == null) {
            fieldMap.put(major_field.getCode, list)
          } else {
            fieldMap.put(majorfield.getCode, list)
          }
          majorMap.put(majorcode, fieldMap)
          deptMap.put(departmentcode, majorMap)
        } else {
          majorMap = deptMap.get(departmentcode).asInstanceOf[Map[_,_]]
          if (majorMap.get(majorcode) == null) {
            fieldMap = new TreeMap[String, ArrayList[Adminclass]]()
            list = new ArrayList[Adminclass]()
            list.add(ac)
            if (majorfield == null) {
              fieldMap.put(major_field.getCode, list)
            } else {
              fieldMap.put(majorfield.getCode, list)
            }
            majorMap.put(majorcode, fieldMap)
            deptMap.put(departmentcode, majorMap)
          } else {
            fieldMap = majorMap.get(majorcode)
            if (majorfield == null) {
              majorfield = major_field
            } else {
            }
            if (fieldMap.get(majorfield.getCode) == null) {
              list = new ArrayList[Adminclass]()
              list.add(ac)
              fieldMap.put(majorfield.getCode, list)
              majorMap.put(majorcode, fieldMap)
              deptMap.put(departmentcode, majorMap)
            } else {
              list = fieldMap.get(majorfield.getCode)
              list.add(ac)
              fieldMap.put(majorfield.getCode, list)
              majorMap.put(majorcode, fieldMap)
              deptMap.put(departmentcode, majorMap)
            }
          }
        }
      }
    }
    val xueyuan = deptMap.keySet
    var sum = 0
    var sum2 = 0
    val renshu = new HashMap()
    val majornum = new HashMap()
    val iter = xueyuan.iterator()
    while (iter.hasNext) {
      val s = iter.next().asInstanceOf[String]
      val zyMap = deptMap.get(s)
      val zhuanye = zyMap.keySet
      val iter2 = zhuanye.iterator()
      while (iter2.hasNext) {
        val s2 = iter2.next().asInstanceOf[String]
        val zyfxMap = zyMap.get(s2).asInstanceOf[Map[_,_]]
        val zyfx = zyfxMap.keySet
        val iter3 = zyfx.iterator()
        while (iter3.hasNext) {
          val s3 = iter3.next().asInstanceOf[String]
          val l = zyfxMap.get(s3).asInstanceOf[List[_]]
          sum = sum + l.size
          sum2 = sum2 + l.size
        }
        majornum.put(s2, sum2)
        sum2 = 0
      }
      renshu.put(s, sum)
      sum = 0
    }
    put("deptmap", deptMap)
    put("majornum", majornum)
    put("renshu", renshu)
    put("mwnum", mwnum)
    forward()
  }

  protected def buildEntityImporter(): EntityImporter = {
    val upload = "importFile"
    try {
      val files = ActionContext.getContext.getParameters.get(upload).asInstanceOf[Array[File]]
      if (files == null || files.length < 1) {
        logger.error("cannot get {} file.", upload)
      }
      val fileName = get(upload + "FileName")
      val is = new FileInputStream(files(0))
      if (fileName.endsWith(".xls")) {
        val wb = new HSSFWorkbook(is)
        if (wb.getNumberOfSheets < 1 || wb.getSheetAt(0).getLastRowNum == 0) {
          return null
        }
        val importer = new MultiEntityImporter()
        importer.setReader(new ExcelItemReader(wb, 1))
        put("importer", importer)
        importer
      } else {
        throw new RuntimeException("donot support other format except excel")
      }
    } catch {
      case e: Exception => {
        logger.error("error", e)
        null
      }
    }
  }

  protected def configImporter(importer: EntityImporter) {
    val mimporter = importer.asInstanceOf[MultiEntityImporter]
    mimporter.addForeignedKeys("name")
    mimporter.addForeignedKeys("code")
    val template = get("templateType")
    if ("std" == template) {
      mimporter.addEntity("student", classOf[Student])
    }
    if ("adminclass" == template) {
      mimporter.addEntity("adminclass", classOf[Adminclass])
    }
    val l = new ImporterForeignerListener(entityDao)
    l.addForeigerKey("name")
    l.addForeigerKey("code")
    val project = entityDao.get(classOf[Project], getSession.get("projectId").asInstanceOf[java.lang.Integer]).asInstanceOf[Project]
    importer.addListener(l).addListener(new AdminclassImportListener(entityDao, template, project))
  }

  def setClassStudentForm(): String = {
    val adminclassId = getInt("adminclassId")
    val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
    put("adminclass", adminclass)
    val builder = OqlBuilder.from(classOf[Student], "student")
    builder.where("student.adminclass=:adminClass", adminclass)
    builder.orderBy("code")
    put("students", entityDao.search(builder))
    forward()
  }

  def addClassStudentList(): String = {
    val builder = OqlBuilder.from(classOf[Student], "std")
    val student = populate(classOf[Student], "student")
    val adminclassId = getInt("adminclassId")
    if (getBoolean("frist") != null && getBoolean("frist")) {
      val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
      student.setGrade(adminclass.grade)
      student.setDepartment(adminclass.department)
      student.setMajor(adminclass.major)
      student.setAdminclass(adminclass)
      builder.where("std.grade = :grade", adminclass.grade)
      builder.where("std.department.name = :department", adminclass.department.getName)
      builder.where("std.major.name = :major", adminclass.major.getName)
    } else {
      if (student.getCode != null) builder.where("std.code like :code", "%" + student.getCode + "%")
      if (student.getName != null) builder.where("std.name like :name", "%" + student.getName + "%")
      if (student.grade != null) builder.where("std.grade like :grade", "%" + student.grade + "%")
      if (student.department != null && student.department.getName != null) builder.where("std.department.name like :department", 
        "%" + student.department.getName + "%")
      if (student.major != null && student.major.getName != null) builder.where("std.major.name like :major", 
        "%" + student.major.getName + "%")
      if (student.getAdminclass != null && student.getAdminclass.getName != null) builder.where("std.adminclass.name like :adminclass", 
        "%" + student.getAdminclass.getName + "%")
    }
    builder.where("std.adminclass is null or std.adminclass.id <> :adminClass", getInt("adminclassId"))
    builder.where("std.department in (:departments)", getDeparts)
    builder.where("std.education in (:educations)", getEducations)
    builder.orderBy("std.code desc")
    builder.limit(getPageLimit)
    val search = entityDao.search(builder)
    put("students", search)
    put("adminclassId", adminclassId)
    put("student", student)
    put("stdCodes", get("stdCodes"))
    forward()
  }

  def addClassStudent(): String = {
    var codes = get("stdCodes")
    if (Strings.isNotEmpty(codes)) {
      codes = codes.replaceAll("[\\s;，；]", ",").replaceAll(",,", ",")
      val projectId = getInt("student.project.id")
      val studentList = new ArrayList[Student]()
      val notAddCodes = new ArrayList[String]()
      val codeArr = Strings.split(codes)
      for (code <- codeArr) {
        val t = entityDao.get(classOf[Student], "code", code)
        var b = false
        if (Collections.isNotEmpty(t)) {
          val std = t.get(0)
          if (std.getProject.id == projectId && !studentList.contains(std)) {
            studentList.add(std)
            b = true
          }
        } else {
          val t1 = entityDao.search(OqlBuilder.from(classOf[Student], "c").where("c.name like :name", 
            "%" + code.trim() + "%"))
          for (std <- t1 if std.getProject.id == projectId && !studentList.contains(std)) {
            studentList.add(std)
            b = true
          }
        }
        if (!b) {
          notAddCodes.add(code)
        }
      }
      put("studentList", studentList)
      put("notAddCodes", notAddCodes)
    }
    forward()
  }

  def saveClassStudent(): String = {
    val adminclassId = getInt("adminclassId")
    val adminclasses = new ArrayList[Adminclass]()
    val adminclassCur = entityDao.get(classOf[Adminclass], adminclassId)
    adminclasses.add(adminclassCur)
    val removeIds = get("studentRemoveIds")
    if (Strings.isNotEmpty(removeIds)) {
      val studentRemoveIds = Strings.split(removeIds)
      val stdIds = new ArrayList[Long]()
      for (studentRemoveId <- studentRemoveIds) {
        try {
          stdIds.add(java.lang.Long.parseLong(studentRemoveId))
        } catch {
          case ex: Exception => 
        }
      }
      val students = entityDao.get(classOf[Student], stdIds)
      for (student <- students) {
        val adminclass = student.getAdminclass
        if (adminclass == null || adminclass.id != adminclassId) {
          //continue
        }
        student.setAdminclass(null)
        entityDao.saveOrUpdate(student)
        entityDao.refresh(adminclass)
        adminclass.setStdCount(adminclass.getStudents.size)
        entityDao.saveOrUpdate(adminclass)
      }
    }
    val stdIds = get("studentIds")
    if (Strings.isNotEmpty(stdIds)) {
      val studentIds = Strings.split(stdIds)
      val students = entityDao.get(classOf[Student], Strings.transformToLong(studentIds))
      val msg = new StringBuffer()
      var errorNum = 0
      for (student <- students) {
        if (student.major != adminclassCur.major || student.direction != adminclassCur.direction) {
          msg.append("\n失败：").append(student.getCode).append(" ")
            .append(student.getName)
          errorNum += 1
        } else {
          val oriAdminclass = student.getAdminclass
          student.setAdminclass(adminclassCur)
          entityDao.saveOrUpdate(student)
          if (oriAdminclass != null) {
            entityDao.refresh(oriAdminclass)
            oriAdminclass.setStdCount(oriAdminclass.getStudents.size)
            entityDao.saveOrUpdate(oriAdminclass)
          }
        }
      }
      if (errorNum > 0) {
        getFlash.put("message", ("\n不符合要求的学生 " + errorNum + "；分别是：") + msg)
      }
    }
    entityDao.refresh(adminclassCur)
    adminclassCur.setStdCount(adminclassCur.getStudents.size)
    entityDao.saveOrUpdate(adminclassCur)
    redirect(new Action(classOf[AdminclassAction], "setClassStudentForm", "&adminclassId=" + adminclassId), 
      "info.save.success")
  }

  def getMajorDuration(): String = {
    val majorId = getInt("majorId")
    val start = getDate("start")
    val response = getResponse
    if (majorId == null || start == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    val major = entityDao.get(classOf[Major], majorId)
    val duration = major.getDuration
    if (major == null || duration == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    val mnum = (duration.floatValue() * 12).toInt
    val c = Calendar.getInstance
    c.setTime(start)
    c.add(Calendar.MONTH, mnum)
    val end = c.getTime
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val result = Collections.newMap[Any]
    result.put("invalidOn", sdf.format(end))
    result.put("duration", duration)
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.write(new Gson().toJson(result))
    response.getWriter.close()
    null
  }
}

class AdminclassPropertyExtractor(resource: TextResource) extends DefaultPropertyExtractor(resource) {

  var textResource: TextResource = _

  var journals: List[StudentJournal] = _

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val student = target.asInstanceOf[Student]
    if (property == "studentJournal.status") {
      val journal = searchJournal(student)
      if (journal != null) {
        return journal.getStatus.getName
      }
      ""
    } else {
      super.getPropertyValue(target, property)
    }
  }

  private def searchJournal(std: Student): StudentJournal = {
    journals.find(_.getStd.id == std.id).getOrElse(null)
  }
}
