package org.openurp.edu.eams.teach.service.internal





import org.beangle.commons.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.dao.query.builder.SqlQuery
import org.openurp.base.CourseUnit
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.service.OccupyProcessor
import org.openurp.edu.eams.teach.service.SqlDao
import org.openurp.edu.eams.teach.service.StudentSource
import org.openurp.edu.eams.teach.service.impl.AbstractStdOccupyProvider
import org.openurp.edu.eams.teach.service.wrapper.StdOccupy
import org.openurp.edu.eams.teach.service.wrapper.TimeZone



class CourseTakeOccupyProvider extends AbstractStdOccupyProvider {

  private var sqlDao: SqlDao = _

  def this(semester: Semester, entityDao: EntityDao) {
    this()
    this.semester = semester
    this.entityDao = entityDao
  }

  def getOccupyCount(source: StudentSource, zone: TimeZone): Map[_,_] = {
    val sqls = new ArrayList()
    val tmpTable = "tmp_" + System.currentTimeMillis()
    val createTableSql = "create  table " + tmpTable + "(stdId number(19) primary key)"
    sqls.add(createTableSql)
    for (std <- source.getStudents) {
      val sb = new StringBuilder(80)
      sb.append("insert into ").append(tmpTable)
      sb.append(" values(").append(std.id).append(")")
      sqls.add(sb.toString)
    }
    sqlDao.batchUpdate(sqls)
    val occupyQuery = "select count(*) as c from jxbxs_t jxb, jxrw_t rw where jxb.jxrwid=rw.id " + 
      " and exists(select * from " + 
      tmpTable + 
      " tmpstd where tmpstd.stdId=jxb.xsid)" + 
      " and rw.xnxqid=:semesterId" + 
      " and rw.kclbid not in(93,43)" + 
      " and (exists (select hd.id from pk_kchd_t hd, jszy_t zy where rw.id=hd.jxrwid" + 
      " and hd.jszyid=zy.id and zy.zj=:weekId and zy.qssj<:endTime and zy.jssj>:startTime" + 
      " and bitand(zy.yxzsz, :weekState)>0))"
    val query = new SqlQuery(occupyQuery)
    val params = new HashMap()
    query.setParams(params)
    params.put("semesterId", semester.id)
    val occupis = executeOccupyQuery(query, zone, new OccupyProcessor() {

      def process(weekOccupy: Map[_,_], unit: CourseUnit, datas: List[_]) {
        var count = weekOccupy.get(unit).asInstanceOf[Number]
        if (null == count) {
          count = new java.lang.Integer(0)
        }
        count = new java.lang.Integer(count.intValue() + datas.get(0).asInstanceOf[Number].intValue())
        weekOccupy.put(unit, count)
      }
    })
    sqls.clear()
    sqls.add("drop table " + tmpTable)
    sqlDao.batchUpdate(sqls)
    occupis
  }

  def getOccupyInfo(source: StudentSource, zone: TimeZone): Map[_,_] = {
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.std in (:stds)")
    query.where("take.task.semester=:semester")
    query.where("take.task.courseType.id not in(93,43)")
    query.where("exists( from take.task.courseSchedule.activities activity where" + 
      " activity.roomOccupation.time.weekId=:weekId" + 
      " and activity.roomOccupation.time.start <:endTime" + 
      " and activity.roomOccupation.time.end >:startTime" + 
      " and bitand(activity.roomOccupation.time.state,:weekState)>0 )")
    val params = new HashMap()
    params.put("stds", source.getStudents)
    params.put("semester", semester)
    query.params(params)
    val occupis = executeOccupyQuery(query, zone, new OccupyProcessor() {

      def process(weekOccupy: Map[_,_], unit: CourseUnit, datas: List[_]) {
        var list = weekOccupy.get(unit).asInstanceOf[List[_]]
        if (null == list) {
          list = new ArrayList()
        }
        list.addAll(courseTakeToOccupy(datas))
        weekOccupy.put(unit, list)
      }

      private def courseTakeToOccupy(takes: List[CourseTake]): List[StdOccupy] = {
        var o = new ArrayList[StdOccupy]()
        for (take <- takes) {
          var occupy = new StdOccupy()
          occupy.setStd(take.getStd)
          occupy.setCourse(take.getLesson.getCourse)
          occupy.setRemark("上课")
          o.add(occupy)
        }
        return o
      }
    })
    occupis
  }

  def setSqlDao(sqlDao: SqlDao) {
    this.sqlDao = sqlDao
  }
}
