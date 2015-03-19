package org.openurp.edu.eams.teach.textbook.service





import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Textbook
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.textbook.TextbookOrderLine



trait TextbookOrderLineService {

  def getLessonsHasTextbook(lessons: Iterable[Lesson]): Set[Long]

  def getTextbooksForLesson(lesson: Lesson): List[Textbook]

  def getTextBookMapByLessons(lessons: Iterable[Lesson]): Map[Long, List[Textbook]]

  def getLessonsHasOrderTextBook(lessons: Iterable[Lesson]): Set[Long]

  def getTextbookOrderLinesByLesson(lessonId: Lesson, std: Student): List[TextbookOrderLine]

  def getTextbookOrderLines(std: Student, semester: Semester, lessonId: java.lang.Long): List[TextbookOrderLine]

  def createTextbookOrderLines(lessonId: java.lang.Long, 
      materialNum: Int, 
      semester: Semester, 
      std: Student): List[TextbookOrderLine]

  def createTextbookOrderLines(bookMap: Map[Textbook, Integer], semester: Semester, std: Student): List[TextbookOrderLine]

  def getTextBooks(takes: List[CourseTake]): Map[Lesson, List[Textbook]]

  def getBookLessons(takes: List[CourseTake]): Map[Textbook, Lesson]
}
