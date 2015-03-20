package org.openurp.edu.eams.teach.textbook.service.internal

import java.util.Date



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Textbook
import org.openurp.edu.eams.teach.lesson.CourseMaterial
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonMaterial
import org.openurp.edu.eams.teach.lesson.model.CourseMaterialBean.CourseMaterialStatus
import org.openurp.edu.eams.teach.lesson.model.LessonMaterialBean.LessonMaterialStatus
import org.openurp.edu.eams.teach.textbook.TextbookOrderLine
import org.openurp.edu.eams.teach.textbook.service.TextbookOrderLineCodeGenerator
import org.openurp.edu.eams.teach.textbook.service.TextbookOrderLineService



class TextbookOrderLineServiceImpl extends BaseServiceImpl with TextbookOrderLineService {

  private var textbookOrderLineCodeGenerator: TextbookOrderLineCodeGenerator = _

  def getLessonsHasTextbook(lessons: Iterable[Lesson]): Set[Long] = {
    if (CollectUtils.isEmpty(lessons)) {
      return Collections.emptySet()
    }
    val builder = OqlBuilder.from(classOf[LessonMaterial].name + " lessonMaterial")
    builder.where("lessonMaterial.lesson in(:lessons)", lessons)
      .select("lessonMaterial.lesson.id")
    CollectUtils.newHashSet(entityDao.search(builder))
  }

  def getTextBookMapByLessons(lessons: Iterable[Lesson]): Map[Long, List[Textbook]] = {
    if (CollectUtils.isEmpty(lessons)) {
      return Collections.emptyMap()
    }
    val lessonMaterials = entityDao.get(classOf[LessonMaterial], "lesson", lessons)
    val result = CollectUtils.newHashMap()
    for (lessonMaterial <- lessonMaterials if !lessonMaterial.books.isEmpty) {
      result.put(lessonMaterial.lesson.id, lessonMaterial.books)
    }
    result
  }

  def getTextbooksForLesson(lesson: Lesson): List[Textbook] = {
    if (null == lesson) return Collections.emptyList()
    val lessonMaterials = entityDao.get(classOf[LessonMaterial], "lesson", lesson)
    if (!lessonMaterials.isEmpty) {
      val lessonMaterial = lessonMaterials.get(0)
      if (LessonMaterialStatus.ASSIGNED == lessonMaterial.status) {
        return lessonMaterials.get(0).books
      }
    } else {
      val courseMaterials = entityDao.search(OqlBuilder.from(classOf[CourseMaterial], "courseMaterial")
        .where("courseMaterial.course = :course", lesson.course)
        .where("courseMaterial.semester = :semester", lesson.semester)
        .where("courseMaterial.department = :department", lesson.teachDepart))
      if (!courseMaterials.isEmpty) {
        val courseMaterial = courseMaterials.get(0)
        if (CourseMaterialStatus.ASSIGNED == courseMaterial.status) {
          return courseMaterials.get(0).books
        }
      }
    }
    Collections.emptyList()
  }

  def getLessonsHasOrderTextBook(lessons: Iterable[Lesson]): Set[Long] = {
    if (CollectUtils.isEmpty(lessons)) {
      return Collections.emptySet()
    }
    val builder = OqlBuilder.from(classOf[TextbookOrderLine].name + " orderline," + classOf[LessonMaterial].name + 
      " lessonMaterial")
    builder.join("lessonMaterial.books", "textbook").where("orderline.textbook = textbook")
      .select("lessonMaterial.lesson.id")
    CollectUtils.newHashSet(entityDao.search(builder))
  }

  def getTextbookOrderLinesByLesson(lesson: Lesson, std: Student): List[TextbookOrderLine] = {
    if (null == lesson || null == std) {
      return Collections.emptyList()
    }
    val builder = OqlBuilder.from(classOf[TextbookOrderLine].name + " orderline," + classOf[LessonMaterial].name + 
      " lessonMaterial")
    builder.join("lessonMaterial.books", "textbook").where("orderline.textbook = textbook")
      .where("lessonMaterial.lesson=:lesson", lesson)
    builder.where("orderline.student=:student", std).select("orderline")
    entityDao.search(builder)
  }

  def getTextbookOrderLines(std: Student, semester: Semester, lessonId: java.lang.Long): List[TextbookOrderLine] = {
    val builder = OqlBuilder.from(classOf[TextbookOrderLine], "textBookOrderLine")
    builder.where("textBookOrderLine.student=:student", std)
      .where("textBookOrderLine.semester=:semester", semester)
    builder.where("exists(from " + classOf[LessonMaterial].name + " lessonMaterial " + 
      "join lessonMaterial.books textBook where lessonMaterial.lesson.id=:lessonId and textBook=textBookOrderLine.textbook)", 
      lessonId)
    entityDao.search(builder)
  }

  def createTextbookOrderLines(lessonId: java.lang.Long, 
      materialNum: Int, 
      semester: Semester, 
      std: Student): List[TextbookOrderLine] = {
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val textbooks = getTextbooksForLesson(lesson)
    val textbookOrderLines = CollectUtils.newArrayList()
    val date = new Date()
    for (textbook <- textbooks) {
      val textbookOrderLine = Model.newInstance(classOf[TextbookOrderLine])
      textbookOrderLine.amount=(materialNum * textbook.price)
      textbookOrderLine.quantity=materialNum
      textbookOrderLine.semester=semester
      textbookOrderLine.student=std
      textbookOrderLine.lesson=lesson
      textbookOrderLine.textbook=textbook
      textbookOrderLine.code=textbookOrderLineCodeGenerator.genCode(textbookOrderLine)
      textbookOrderLine.createdAt=date
      textbookOrderLine.updatedAt=date
      textbookOrderLines.add(textbookOrderLine)
    }
    textbookOrderLines
  }

  def createTextbookOrderLines(bookMap: Map[Textbook, Integer], semester: Semester, std: Student): List[TextbookOrderLine] = {
    val textbookOrderLines = CollectUtils.newArrayList()
    val date = new Date()
    val courseTakeQuery = OqlBuilder.from(classOf[CourseTake], "ct")
    courseTakeQuery.where("ct.std=:std and ct.lesson.semester=:semester", std, semester)
    val bookLessons = this.bookLessons(entityDao.search(courseTakeQuery))
    for (textbook <- bookMap.keySet) {
      val textbookOrderLine = Model.newInstance(classOf[TextbookOrderLine])
      textbookOrderLine.amount=(bookMap.get(textbook) * textbook.price)
      textbookOrderLine.quantity=bookMap.get(textbook)
      textbookOrderLine.semester=semester
      textbookOrderLine.student=std
      textbookOrderLine.lesson=bookLessons.get(textbook)
      textbookOrderLine.textbook=textbook
      textbookOrderLine.code=textbookOrderLineCodeGenerator.genCode(textbookOrderLine)
      textbookOrderLine.createdAt=date
      textbookOrderLine.updatedAt=date
      textbookOrderLines.add(textbookOrderLine)
    }
    textbookOrderLines
  }

  def getBookLessons(takes: List[CourseTake]): Map[Textbook, Lesson] = {
    val bookLessons = CollectUtils.newHashMap()
    for (take <- takes) {
      val lesson = take.lesson
      val lessonMaterials = entityDao.get(classOf[LessonMaterial], "lesson", lesson)
      if (!lessonMaterials.isEmpty) {
        val lessonMaterial = lessonMaterials.get(0)
        if (LessonMaterialStatus.ASSIGNED == lessonMaterial.status) {
          val lessonTextbooks = lessonMaterials.get(0).books
          for (textbook <- lessonTextbooks if !bookLessons.containsKey(textbook)) bookLessons.put(textbook, 
            lesson)
        }
      } else {
        val courseMaterials = entityDao.search(OqlBuilder.from(classOf[CourseMaterial], "courseMaterial")
          .where("courseMaterial.course = :course", lesson.course)
          .where("courseMaterial.semester = :semester", lesson.semester)
          .where("courseMaterial.department = :department", lesson.teachDepart)
          .cacheable())
        if (!courseMaterials.isEmpty) {
          val courseMaterial = courseMaterials.get(0)
          if (CourseMaterialStatus.ASSIGNED == courseMaterial.status) {
            val lessonTextbooks = courseMaterials.get(0).books
            for (textbook <- lessonTextbooks if !bookLessons.containsKey(textbook)) bookLessons.put(textbook, 
              lesson)
          }
        }
      }
    }
    bookLessons
  }

  def getTextBooks(takes: List[CourseTake]): Map[Lesson, List[Textbook]] = {
    val textbooksTotal = CollectUtils.newHashSet()
    val lessonBooks = CollectUtils.newHashMap()
    for (take <- takes) {
      val lesson = take.lesson
      val lessonMaterials = entityDao.get(classOf[LessonMaterial], "lesson", lesson)
      if (!lessonMaterials.isEmpty) {
        val lessonMaterial = lessonMaterials.get(0)
        if (LessonMaterialStatus.ASSIGNED == lessonMaterial.status) {
          val lessonTextbooks = lessonMaterials.get(0).books
          val textbooks = CollectUtils.newArrayList()
          for (textbook <- lessonTextbooks if !textbooksTotal.contains(textbook)) {
            textbooks.add(textbook)
            textbooksTotal.add(textbook)
          }
          lessonBooks.put(lesson, textbooks)
        }
      } else {
        val courseMaterials = entityDao.search(OqlBuilder.from(classOf[CourseMaterial], "courseMaterial")
          .where("courseMaterial.course = :course", lesson.course)
          .where("courseMaterial.semester = :semester", lesson.semester)
          .where("courseMaterial.department = :department", lesson.teachDepart))
        if (!courseMaterials.isEmpty) {
          val courseMaterial = courseMaterials.get(0)
          if (CourseMaterialStatus.ASSIGNED == courseMaterial.status) {
            val lessonTextbooks = courseMaterials.get(0).books
            val textbooks = CollectUtils.newArrayList()
            for (textbook <- lessonTextbooks if !textbooksTotal.contains(textbook)) {
              textbooks.add(textbook)
              textbooksTotal.add(textbook)
            }
            lessonBooks.put(lesson, textbooks)
          }
        }
      }
    }
    lessonBooks
  }

  def setTextbookOrderLineCodeGenerator(textbookOrderLineCodeGenerator: TextbookOrderLineCodeGenerator) {
    this.textbookOrderLineCodeGenerator = textbookOrderLineCodeGenerator
  }
}
