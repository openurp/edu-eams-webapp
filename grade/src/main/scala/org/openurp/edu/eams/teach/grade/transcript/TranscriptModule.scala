package org.openurp.edu.eams.teach.grade.transcript

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.transcript.service.impl.SpringTranscriptDataProviderRegistry
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptGpaProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptOtherGradeProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptPlanCourseProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptPublishedGradeProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptStdExamineeProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptStdGraduationProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.TranscriptThesisProvider
import org.openurp.edu.eams.teach.grade.transcript.web.action.FinalAction



class TranscriptModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[FinalAction])
    bind(classOf[TranscriptPlanCourseProvider], classOf[TranscriptGpaProvider], classOf[TranscriptPublishedGradeProvider], 
      classOf[TranscriptOtherGradeProvider], classOf[TranscriptStdGraduationProvider], classOf[TranscriptStdExamineeProvider], 
      classOf[TranscriptThesisProvider], classOf[SpringTranscriptDataProviderRegistry])
      .shortName()
  }
}
