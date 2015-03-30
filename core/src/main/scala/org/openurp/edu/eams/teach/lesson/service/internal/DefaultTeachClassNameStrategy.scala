package org.openurp.edu.eams.teach.lesson.service.internal

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import collection.mutable.Buffer
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta._
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProviderFactory
import DefaultTeachClassNameStrategy._
import org.beangle.commons.entity.metadata.Model
import javax.validation.constraints.Size

object DefaultTeachClassNameStrategy {

  private var fullnameMaxSize: java.lang.Integer = _

  private var nameMaxSize: java.lang.Integer = _
}

class DefaultTeachClassNameStrategy extends TeachClassNameStrategy {

  protected var logger: Logger = LoggerFactory.getLogger(getClass)

  var lessonLimitItemContentProviderFactory: LessonLimitItemContentProviderFactory = _

  override def genName(groups: Seq[LessonLimitGroup]): String = {
    Strings.abbreviate(buildAll(groups)._1, getNameMaxSize)
  }

  def genName(teachClass: TeachClass): String = genName(teachClass.limitGroups)

  def genName(fullname: String): String = {
    if (Strings.isBlank(fullname)) {
      return fullname
    }
    Strings.abbreviate(fullname, getNameMaxSize)
  }

  def abbreviateName(teachClass: TeachClass) {
    if (null != teachClass && Strings.isBlank(teachClass.name)) {
      teachClass.name = Strings.abbreviate(teachClass.name, getNameMaxSize)
    }
  }

  def genFullname(groups: Seq[LessonLimitGroup]): String = {
    Strings.abbreviate(buildAll(groups)._2, getFullnameMaxSize)
  }

  def genFullname(teachClass: TeachClass): String = genFullname(teachClass.limitGroups)

  def autoName(teachClass: TeachClass) {
    val names = buildAll(teachClass.limitGroups)
    teachClass.name = names._1
    teachClass.fullname = names._2
  }

  private def buildAll(groups: Seq[LessonLimitGroup]): Pair[String, String] = {
    val providers = Collections.newMap[LimitMeta, LessonLimitItemContentProvider[_]]
    val groupContentTitles = Collections.newBuffer[collection.Map[LimitMeta, Pair[Operator, collection.Set[String]]]]
    val excludeContents = Collections.newMap[LimitMeta, Buffer[Any]]
    for (lessonLimitGroup <- groups) {
      val metaContentTitles = Collections.newMap[LimitMeta, Pair[Operator, collection.Set[String]]]
      for (item <- lessonLimitGroup.items) {
        val op = item.operator
        val meta = item.meta
        var provider = providers.get(meta).orNull
        if (null == provider) {
          provider = lessonLimitItemContentProviderFactory.getProvider(meta)
          providers.put(meta, provider)
        }
        val contentIdTitles = provider.getContentIdTitleMap(item.content)
        val contentTitles = new collection.mutable.LinkedHashSet[String]
        contentTitles ++= (contentIdTitles.values)
        if (Operators.NOT_EQUAL == op || Operators.NOT_IN == op) {
          var oneMetaExcludeContents = excludeContents.get(meta).orNull
          if (null == oneMetaExcludeContents) {
            oneMetaExcludeContents = Collections.newBuffer[Any]
            excludeContents.put(meta, oneMetaExcludeContents)
          }
          oneMetaExcludeContents += contentTitles
        }
        metaContentTitles.put(meta, new Pair[Operator, collection.Set[String]](op, contentTitles))
      }
      groupContentTitles += metaContentTitles
    }
    for (oneGroupContentTitles <- groupContentTitles; (key, value) <- oneGroupContentTitles) {
      val metaId = key
      val op = value._1
      if (Operators.Equals == op || Operators.IN == op) {
        val contents = value._2
        val oneMetaExcludeContents = excludeContents.get(metaId)
        if (null != oneMetaExcludeContents) {
          for (oneMetaExcludeContentSet <- oneMetaExcludeContents) {
            oneMetaExcludeContentSet --= contents
          }
        }
      }
    }
    val fullNameBuilder = new StringBuilder()
    val nameBuilder = new StringBuilder()
    //    val metasEnums = Collections.newMap[LimitMeta,]
    //    for (lessonLimitMetaEnum <- LessonLimitMeta.values) {
    //      metasEnums.put(lessonLimitMetaEnum.id, lessonLimitMetaEnum)
    //    }
    val metaTitles = Collections.newMap[LimitMeta, String]
    metaTitles.put(LessonLimitMeta.Adminclass, "班级")
    metaTitles.put(LessonLimitMeta.Department, "院系")
    metaTitles.put(LessonLimitMeta.Direction, "方向")
    metaTitles.put(LessonLimitMeta.Education, "学历层次")
    metaTitles.put(LessonLimitMeta.Gender, "性别")
    metaTitles.put(LessonLimitMeta.Grade, "年级")
    metaTitles.put(LessonLimitMeta.Major, "专业")
    metaTitles.put(LessonLimitMeta.Program, "计划")
    metaTitles.put(LessonLimitMeta.StdLabel, "学生标签")
    metaTitles.put(LessonLimitMeta.StdType, "学生类别")
    for (oneGroupContentTitles <- groupContentTitles) {
      var isEmptyGroup = true
      for ((meta, value) <- oneGroupContentTitles) {
        val length = fullNameBuilder.length
        if (LessonLimitMeta.Grade == meta) {
          appendGradeContents(fullNameBuilder, oneGroupContentTitles)
        } else {
          appendEntityContents(fullNameBuilder, meta, oneGroupContentTitles, metaTitles.get(meta).orNull)
        }
        isEmptyGroup = length == fullNameBuilder.length
      }
      if (!isEmptyGroup) {
        fullNameBuilder.append(";")
      }
      val sb = new StringBuilder()
      appendEntityContents(sb, LessonLimitMeta.Adminclass, oneGroupContentTitles, "班级")
      if (sb.length == 0) {
        appendGradeContents(sb, oneGroupContentTitles)
        val containsMajor = containsMeta(LessonLimitMeta.Major, oneGroupContentTitles)
        if (containsMajor) {
          appendEntityContents(sb, LessonLimitMeta.Major, oneGroupContentTitles, "专业")
        } else {
          appendEntityContents(sb, LessonLimitMeta.Department, oneGroupContentTitles, "院系")
        }
        appendEntityContents(sb, LessonLimitMeta.StdType, oneGroupContentTitles, "方向")
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

  private def containsMeta(meta: LimitMeta, groupContents: collection.Map[LimitMeta, Pair[Operator, collection.Set[String]]]): Boolean = {
    val pair = groupContents.get(meta).orNull
    if (null != pair) {
      return Collections.isNotEmpty(pair._2)
    }
    false
  }

  private def appendEntityContents(sb: StringBuilder,
    meta: LimitMeta,
    oneGroupContentTitles: collection.Map[LimitMeta, Pair[Operator, collection.Set[String]]],
    key: String): StringBuilder = {
    val directionPair = oneGroupContentTitles.get(meta).orNull
    if (null != directionPair) {
      val contents = directionPair._2
      if (Collections.isNotEmpty(contents)) {
        if (sb.length > 0) {
          sb.append(",")
        }
        sb.append(key).append(":")
        val directionOp = directionPair._1
        if (directionOp == LessonLimitMeta.Operators.NOT_EQUAL || directionOp == LessonLimitMeta.Operators.NOT_IN) {
          sb.append("非 ")
        }
        sb.append(Strings.join(contents.toArray, " "))
      }
    }
    sb
  }

  private def appendGradeContents(sb: StringBuilder, oneGroupContentTitles: collection.Map[LimitMeta, Pair[Operator, collection.Set[String]]]): StringBuilder = {
    val gradePair = oneGroupContentTitles.get(LessonLimitMeta.Grade).orNull
    if (null != gradePair) {
      if (Collections.isNotEmpty(gradePair._2)) {
        if (sb.length > 0) {
          sb.append(",")
        }
        sb.append("年级:")
        val gradeOp = gradePair._1
        if (gradeOp == LessonLimitMeta.Operators.NOT_EQUAL || gradeOp == LessonLimitMeta.Operators.NOT_IN) {
          sb.append("非 ")
        }
        for (grade <- gradePair._2) {
          sb.append(grade).append("级 ")
        }
        sb.deleteCharAt(sb.length - 1)
        if (gradeOp == LessonLimitMeta.Operators.GREATE_EQUAL_THAN) {
          sb.append(" 及低年级")
        } else if (gradeOp == LessonLimitMeta.Operators.LESS_EQUAL_THAN) {
          sb.append(" 及高年级")
        }
      }
    }
    sb
  }

  private def getFullnameMaxSize(): Int = {
    if (null == fullnameMaxSize) {
      fullnameMaxSize = 600
      val entityClass = Model.getType(classOf[TeachClassBean]).entityClass
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
      val entityClass = Model.getType(classOf[TeachClassBean]).entityClass
      try {
        nameMaxSize = entityClass.getDeclaredField("name").getAnnotation(classOf[Size]).max()
      } catch {
        case e: NoSuchFieldException => logger.info("get " + entityClass.getName + ".name max size failure",
          e)
        case e: SecurityException => logger.info("get " + entityClass.getName + ".name max size failure",
          e)
      }
    }
    nameMaxSize
  }
}
