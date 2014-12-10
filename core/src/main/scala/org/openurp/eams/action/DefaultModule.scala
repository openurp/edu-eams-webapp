package org.openurp.eams.action

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.action.code.DisciplineCatalogAction
import org.openurp.eams.action.code.CourseTakeTypeAction
import org.openurp.eams.action.code.CourseCategoryAction
import org.openurp.eams.action.code.TutorTypeAction
import org.openurp.eams.action.code.StdStatusAction
import org.openurp.eams.action.code.StdLabelTypeAction
import org.openurp.eams.action.code.DisciplineAction
import org.openurp.eams.action.code.TeacherTitleAction
import org.openurp.eams.action.code.TeacherTitleLevelAction
import org.openurp.eams.action.code.StudyTypeAction
import org.openurp.eams.action.code.DegreeAction
import org.openurp.eams.action.code.ExamTypeAction
import org.openurp.eams.action.code.TeacherUnitTypeAction
import org.openurp.eams.action.code.ScoreMarkStyleAction
import org.openurp.eams.action.code.StdTypeAction
import org.openurp.eams.action.code.CourseAbilityRateAction
import org.openurp.eams.action.code.GradeTypeAction
import org.openurp.eams.action.code.ExamStatusAction
import org.openurp.eams.action.code.StdLabelAction
import org.openurp.eams.action.code.ExamModeAction
import org.openurp.eams.action.code.CourseTypeAction
import org.openurp.eams.action.code.TeacherTypeAction
import org.openurp.eams.action.code.TeacherStateAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[TeacherTypeAction], classOf[TeacherStateAction], classOf[TeacherUnitTypeAction], classOf[TutorTypeAction], classOf[StdStatusAction])
    bind(classOf[DisciplineAction], classOf[DisciplineCatalogAction])
    bind(classOf[TeacherTitleAction], classOf[TeacherTitleLevelAction], classOf[StudyTypeAction], classOf[DegreeAction])
    bind(classOf[StdLabelAction], classOf[StdLabelTypeAction], classOf[StdTypeAction])
    bind(classOf[AdminclassAction], classOf[MajorAction], classOf[DirectionAction], classOf[DirectionJournalAction], classOf[MajorJournalAction])
    bind(classOf[ProjectAction], classOf[ProjectCodeAction], classOf[ProjectClassroomAction])
    bind(classOf[StudentAction], classOf[StudentJournalAction])
    bind(classOf[ExamModeAction], classOf[ExamStatusAction], classOf[ExamTypeAction])
    bind(classOf[CourseAbilityRateAction], classOf[CourseCategoryAction], classOf[CourseHourAction], classOf[CourseTakeTypeAction], classOf[CourseTypeAction])
    bind(classOf[GradeTypeAction], classOf[ScoreMarkStyleAction])
    bind(classOf[CourseAction], classOf[CourseGradeAction], classOf[CourseHourAction], classOf[ExamGradeAction])
    bind(classOf[StdGradeReportAction])
    bind(classOf[LessonGradeReportAction])
  }
}