package org.openurp.edu.eams.teach.program.service


import org.openurp.edu.base.Program




@SerialVersionUID(-6050167055646192463L)
class AmbiguousMajorProgramException( val ambiguousPrograms: List[Program])
    extends Exception()
