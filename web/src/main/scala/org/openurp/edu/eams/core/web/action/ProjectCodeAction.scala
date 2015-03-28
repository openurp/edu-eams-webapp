package org.openurp.edu.eams.core.web.action



import org.apache.commons.lang3.ClassUtils
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.pojo.BaseCode
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.model.CodeCategory
import org.beangle.ems.dictionary.model.CodeMeta
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.model.ProjectCodeBean
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class ProjectCodeAction extends ProjectSupportAction {

  def index(): String = {
    put("coders", getCodeMeta(classOf[Class[_]]))
    put("categories", entityDao.getAll(classOf[CodeCategory]))
    forward()
  }

  def edit(): String = {
    var projectId = getIntId("projectx")
    if (projectId == null) {
      projectId = getProject.id
    }
    try {
      val className = get("className")
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      put("shortName", shortName)
      put("className", className)
      val query = OqlBuilder.from(classOf[CodeMeta], "codeMeta")
      populateConditions(query)
      query.where("codeMeta.className = :className", className)
      val codeMetaList = entityDao.search(query)
      put("codeMeta", codeMetaList.get(0))
      val cttlist = getAvailableCodes(Class.forName(className), shortName, projectId)
      put("availableCodes", cttlist)
      put("projectCodes", getProjectCodes(Class.forName(className), projectId))
      put("projects", entityDao.getAll(classOf[Project]))
      put("projectx", entityDao.get(classOf[Project], projectId))
      val baseCodeObj = getEntity(Class.forName(className), "baseCode").asInstanceOf[BaseCode[_]]
      put("baseCode", baseCodeObj)
      forward()
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
  }

  def save(): String = {
    val projectId = getIntId("projectx")
    val addCodeIds = getIntIds("addCode")
    val delCodeIds = getIntIds("delCode")
    val pc = new ArrayList[ProjectCodeBean]()
    val className = get("className")
    for (id <- addCodeIds) {
      val projectCode = new ProjectCodeBean()
      val codeMeats = getCodeMeta(Class.forName(className))
      if (Collections.isNotEmpty(codeMeats)) {
        projectCode.setMeta(codeMeats.get(0))
      }
      projectCode.setProject(entityDao.get(classOf[Project], projectId))
      projectCode.setCodeId(id)
      pc.add(projectCode)
    }
    entityDao.saveOrUpdate(pc)
    val sizes = delCodeIds.length
    if (sizes > 0) {
      var str = ""
      for (i <- 0 until sizes) {
        str = if (i == sizes - 1) str + "?" else str + "?,"
      }
      val codeMeta = entityDao.get(classOf[CodeMeta], "className", className)
        .get(0)
      entityDao.executeUpdate("delete from " + classOf[ProjectCodeBean].getName + " pc " + 
        "where pc.codeId in(" + 
        str + 
        ") and pc.project.id = " + 
        projectId + 
        " and pc.meta.id=" + 
        codeMeta.id, delCodeIds)
    }
    redirect("edit", "info.action.success")
  }

  private def getAvailableCodes(classzz: Class[_], shortName: String, projectId: java.lang.Integer): List[_] = {
    val builder = OqlBuilder.from(classzz, shortName).where(shortName + ".effectiveAt <= :now and (" + shortName + 
      ".invalidAt is null or " + 
      shortName + 
      ".invalidAt >= :now)", new java.util.Date())
      .where("not exists(from " + classOf[ProjectCodeBean].getName + 
      " pb where pb.meta.className =:className  and pb.codeId = " + 
      shortName + 
      ".id and pb.project.id =:projectId)", classzz.getName, projectId)
    entityDao.search(builder)
  }

  private def getProjectCodes(classzz: Class[_], projectId: java.lang.Integer): List[_] = {
    val query = OqlBuilder.from(classzz.getName + " zz")
    query.where("exists(from " + classOf[ProjectCodeBean].getName + 
      " pb where pb.meta.className =:className and pb.project.id =:projectId and pb.codeId = zz.id)", 
      classzz.getName, projectId)
    entityDao.search(query)
  }

  private def getCodeMeta(classzz: Class[_]): List[CodeMeta] = {
    val query = OqlBuilder.from(classOf[CodeMeta], "codeMeta")
    if (classzz.getName != "java.lang.Class") query.where("codeMeta.className =:className", classzz.getName)
    entityDao.search(query)
  }
}
