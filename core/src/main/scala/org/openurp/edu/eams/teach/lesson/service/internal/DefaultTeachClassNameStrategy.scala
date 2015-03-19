package org.openurp.edu.eams.teach.lesson.service.internal

import java.util.LinkedHashSet


.Entry

import javax.validation.constraints.Size
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProviderFactory
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import DefaultTeachClassNameStrategy._



object DefaultTeachClassNameStrategy {

  private var fullnameMaxSize: java.lang.Integer = _

  private var nameMaxSize: java.lang.Integer = _
}

class DefaultTeachClassNameStrategy extends TeachClassNameStrategy {

  protected var logger: Logger = LoggerFactory.getLogger(getClass)

  private var courseLimitItemContentProviderFactory: CourseLimitItemContentProviderFactory = _

  def genName(groups: List[CourseLimitGroup]): String = {
    Strings.abbreviate(buildAll(groups).getLeft, getNameMaxSize)
  }

  def genName(teachClass: TeachClass): String = genName(teachClass.getLimitGroups)

  def genName(fullname: String): String = {
    if (Strings.isBlank(fullname)) {
      return fullname
    }
    Strings.abbreviate(fullname, getNameMaxSize)
  }

  def abbreviateName(teachClass: TeachClass) {
    if (null != teachClass && Strings.isBlank(teachClass.getName)) {
      teachClass.setName(Strings.abbreviate(teachClass.getName, getNameMaxSize))
    }
  }

  def genFullname(groups: List[CourseLimitGroup]): String = {
    Strings.abbreviate(buildAll(groups).getRight, getFullnameMaxSize)
  }

  def genFullname(teachClass: TeachClass): String = genFullname(teachClass.getLimitGroups)

  def autoName(teachClass: TeachClass) {
    val names = buildAll(teachClass.getLimitGroups)
    teachClass.setName(names.getLeft)
    teachClass.setFullname(names.getRight)
  }

  private def buildAll(groups: List[CourseLimitGroup]): Pair[String, String] = {
    val providers = CollectUtils.newHashMap()
    val groupContentTitles = CollectUtils.newArrayList()
    val excludeContents = CollectUtils.newHashMap()
    for (courseLimitGroup <- groups) {
      val metaContentTitles = CollectUtils.newHashMap()
      for (item <- courseLimitGroup.getItems) {
        val op = item.getOperator
        val meta = item.getMeta
        var provider = providers.get(meta.id)
        if (null == provider) {
          provider = courseLimitItemContentProviderFactory.getProvider(meta)
          providers.put(meta.id, provider)
        }
        val contentIdTitles = provider.getContentIdTitleMap(item.getContent)
        val contentTitles = new LinkedHashSet[String](contentIdTitles.values)
        if (Operator.NOT_EQUAL == op || Operator.NOT_IN == op) {
          var oneMetaExcludeContents = excludeContents.get(meta.id)
          if (null == oneMetaExcludeContents) {
            oneMetaExcludeContents = CollectUtils.newArrayList()
            excludeContents.put(meta.id, oneMetaExcludeContents)
          }
          oneMetaExcludeContents.add(contentTitles)
        }
        metaContentTitles.put(meta.id, new Pair[CourseLimitMeta.Operator, Set[String]](op, contentTitles))
      }
      groupContentTitles.add(metaContentTitles)
    }
    for (oneGroupContentTitles <- groupContentTitles; (key, value) <- oneGroupContentTitles) {
      val metaId = key
      val op = value.getLeft
      if (Operator.EQUAL == op || Operator.IN == op) {
        val contents = value.getRight
        val oneMetaExcludeContents = excludeContents.get(metaId)
        if (null != oneMetaExcludeContents) {
          for (oneMetaExcludeContentSet <- oneMetaExcludeContents) {
            oneMetaExcludeContentSet.removeAll(contents)
          }
        }
      }
    }
    val fullNameBuilder = new StringBuilder()
    val nameBuilder = new StringBuilder()
    val enums = CourseLimitMetaEnum.values
    val metasEnums = CollectUtils.newHashMap()
    for (courseLimitMetaEnum <- enums) {
      metasEnums.put(courseLimitMetaEnum.getMetaId, courseLimitMetaEnum)
    }
    val metaTitles = CollectUtils.newHashMap()
    metaTitles.put(CourseLimitMetaEnum.ADMINCLASS, "班级")
    metaTitles.put(CourseLimitMetaEnum.DEPARTMENT, "院系")
    metaTitles.put(CourseLimitMetaEnum.DIRECTION, "方向")
    metaTitles.put(CourseLimitMetaEnum.EDUCATION, "学历层次")
    metaTitles.put(CourseLimitMetaEnum.GENDER, "性别")
    metaTitles.put(CourseLimitMetaEnum.GRADE, "年级")
    metaTitles.put(CourseLimitMetaEnum.MAJOR, "专业")
    metaTitles.put(CourseLimitMetaEnum.NORMALCLASS, "常规教学班")
    metaTitles.put(CourseLimitMetaEnum.PROGRAM, "计划")
    metaTitles.put(CourseLimitMetaEnum.STDLABEL, "学生标签")
    metaTitles.put(CourseLimitMetaEnum.STDTYPE, "学生类别")
    for (oneGroupContentTitles <- groupContentTitles) {
      var isEmptyGroup = true
      for ((key, value) <- oneGroupContentTitles) {
        val metaId = key
        val metaEnum = metasEnums.get(metaId)
        val length = fullNameBuilder.length
        if (CourseLimitMetaEnum.GRADE == metaEnum) {
          appendGradeContents(fullNameBuilder, oneGroupContentTitles)
        } else {
          appendEntityContents(fullNameBuilder, metaEnum, oneGroupContentTitles, metaTitles.get(metaEnum))
        }
        isEmptyGroup = length == fullNameBuilder.length
      }
      if (!isEmptyGroup) {
        fullNameBuilder.append(";")
      }
      val sb = new StringBuilder()
      appendEntityContents(sb, CourseLimitMetaEnum.ADMINCLASS, oneGroupContentTitles, "班级")
      if (sb.length == 0) {
        appendGradeContents(sb, oneGroupContentTitles)
        val containsMajor = containsMeta(CourseLimitMetaEnum.MAJOR, oneGroupContentTitles)
        if (containsMajor) {
          appendEntityContents(sb, CourseLimitMetaEnum.MAJOR, oneGroupContentTitles, "专业")
        } else {
          appendEntityContents(sb, CourseLimitMetaEnum.DEPARTMENT, oneGroupContentTitles, "院系")
        }
        appendEntityContents(sb, CourseLimitMetaEnum.STDTYPE, oneGroupContentTitles, "方向")
      }
      if (sb.length > 0) {
        if (nameBuilder.length > 0) {
          nameBuilder.append(";")
        }
        nameBuilder.append(sb.toString)
      }
    }
    if (nameBuilder.length == 0) {
      nameBuilder.append("全校")
    }
    val name = nameBuilder.toString
    var fullname = "全校"
    if (fullNameBuilder.length > 0) {
      fullname = fullNameBuilder.substring(0, fullNameBuilder.length - 1)
    }
    new Pair[String, String](name, fullname)
  }

  private def containsMeta(meta: CourseLimitMetaEnum, groupContents: Map[Long, Pair[Operator, Set[String]]]): Boolean = {
    val pair = groupContents.get(meta.getMetaId)
    if (null != pair) {
      return CollectUtils.isNotEmpty(pair.getRight)
    }
    false
  }

  private def appendEntityContents(sb: StringBuilder, 
      meta: CourseLimitMetaEnum, 
      oneGroupContentTitles: Map[Long, Pair[Operator, Set[String]]], 
      key: String): StringBuilder = {
    val directionPair = oneGroupContentTitles.get(meta.getMetaId)
    if (null != directionPair) {
      val contents = directionPair.getRight
      if (CollectUtils.isNotEmpty(contents)) {
        if (sb.length > 0) {
          sb.append(",")
        }
        sb.append(key).append(":")
        val directionOp = directionPair.getLeft
        if (directionOp == CourseLimitMeta.Operator.NOT_EQUAL || directionOp == CourseLimitMeta.Operator.NOT_IN) {
          sb.append("非 ")
        }
        sb.append(Strings.join(contents.toArray(Array.ofDim[String](contents.size)), " "))
      }
    }
    sb
  }

  private def appendGradeContents(sb: StringBuilder, oneGroupContentTitles: Map[Long, Pair[Operator, Set[String]]]): StringBuilder = {
    val gradePair = oneGroupContentTitles.get(CourseLimitMetaEnum.GRADE.getMetaId)
    if (null != gradePair) {
      if (CollectUtils.isNotEmpty(gradePair.getRight)) {
        if (sb.length > 0) {
          sb.append(",")
        }
        sb.append("年级:")
        val gradeOp = gradePair.getLeft
        if (gradeOp == CourseLimitMeta.Operator.NOT_EQUAL || gradeOp == CourseLimitMeta.Operator.NOT_IN) {
          sb.append("非 ")
        }
        for (grade <- gradePair.getRight) {
          sb.append(grade).append("级 ")
        }
        sb.deleteCharAt(sb.length - 1)
        if (gradeOp == CourseLimitMeta.Operator.GREATE_EQUAL_THAN) {
          sb.append(" 及低年级")
        } else if (gradeOp == CourseLimitMeta.Operator.LESS_EQUAL_THAN) {
          sb.append(" 及高年级")
        }
      }
    }
    sb
  }

  private def getFullnameMaxSize(): Int = {
    if (null == fullnameMaxSize) {
      fullnameMaxSize = 600
      val entityClass = Model.getType(classOf[TeachClassBean]).getEntityClass
      try {
        fullnameMaxSize = entityClass.getDeclaredField("fullname").getAnnotation(classOf[Size])
          .max()
      } catch {
        case e: NoSuchFieldException => logger.info("get " + entityClass.getName + ".name max size failure", 
          e)
        case e: SecurityException => logger.info("get " + entityClass.getName + ".name max size failure", 
          e)
      }
    }
    fullnameMaxSize
  }

  private def getNameMaxSize(): Int = {
    if (null == nameMaxSize) {
      nameMaxSize = 100
      val entityClass = Model.getType(classOf[TeachClassBean]).getEntityClass
      try {
        nameMaxSize = entityClass.getDeclaredField("name").getAnnotation(classOf[Size])
          .max()
      } catch {
        case e: NoSuchFieldException => logger.info("get " + entityClass.getName + ".name max size failure", 
          e)
        case e: SecurityException => logger.info("get " + entityClass.getName + ".name max size failure", 
          e)
      }
    }
    nameMaxSize
  }

  def setCourseLimitItemContentProviderFactory(courseLimitItemContentProviderFactory: CourseLimitItemContentProviderFactory) {
    this.courseLimitItemContentProviderFactory = courseLimitItemContentProviderFactory
  }
}
