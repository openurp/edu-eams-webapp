package org.openurp.edu.eams.system.security.web.action

import java.util.Comparator



import java.util.TreeSet
import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation.Builder
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.functor.Predicate
import org.beangle.ems.security.helper.ProfileHelper
import org.beangle.security.blueprint.Field
import org.beangle.security.blueprint.Profile
import org.beangle.security.blueprint.Property
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.model.UserBean
import org.beangle.security.blueprint.model.UserProfileBean
import org.beangle.security.blueprint.service.ProfileService
import org.beangle.struts2.annotation.Action
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.DirectionJournal
import org.openurp.edu.base.Major
import org.openurp.edu.base.MajorJournal
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType



@Action("/security/profile")
class ProfileAction extends org.beangle.ems.security.web.action.ProfileAction {

  def edit(): String = {
    val isAdmin = isAdmin
    val mngFields = CollectUtils.newHashMap()
    val aoFields = CollectUtils.newHashMap()
    val profileService = securityHelper.getProfileService
    val _fs = extractFields()
    val projectField = _fs(0)
    val educationField = _fs(1)
    val stdTypeField = _fs(2)
    val departField = _fs(3)
    val majorField = _fs(4)
    val directionField = _fs(5)
    val mngProfiles = entityDao.get(classOf[User], getUserId).getProfiles
    var mngProjects = CollectUtils.newArrayList()
    if (isAdmin) {
      mngProjects = entityDao.getAll(classOf[Project])
      for (mngProject <- mngProjects) {
        mngFields.put(mngProject.id + "_educations", mngProject.educations)
        mngFields.put(mngProject.id + "_stdTypes", mngProject.getTypes)
        mngFields.put(mngProject.id + "_departs", mngProject.departments)
        mngFields.put(mngProject.id + "_majors", entityDao.get(classOf[Major], "project.id", mngProject.id))
        mngFields.put(mngProject.id + "_directions", entityDao.get(classOf[Direction], "major.project.id", 
          mngProject.id))
      }
    } else {
      for (mngProfile <- mngProfiles) {
        val projects = getMyProfileValues(Collections.singletonList(mngProfile), projectField).asInstanceOf[List[_]]
        val educations = getMyProfileValues(Collections.singletonList(mngProfile), educationField)
        val stdTypes = getMyProfileValues(Collections.singletonList(mngProfile), stdTypeField)
        val departs = getMyProfileValues(Collections.singletonList(mngProfile), departField)
        val majors = getMyProfileValues(Collections.singletonList(mngProfile), majorField)
        val directions = getMyProfileValues(Collections.singletonList(mngProfile), directionField)
        for (mngProject <- projects) {
          if (!mngProjects.contains(mngProject)) {
            mngProjects.add(mngProject)
          }
          mngFields.put(mngProject.id + "_educations", CollectUtils.intersection(mngProject.educations, 
            educations))
          mngFields.put(mngProject.id + "_stdTypes", CollectUtils.intersection(mngProject.getTypes, 
            stdTypes))
          mngFields.put(mngProject.id + "_departs", CollectUtils.intersection(mngProject.departments, 
            departs))
          mngFields.put(mngProject.id + "_majors", CollectUtils.intersection(entityDao.get(classOf[Major], 
            "project.id", mngProject.id), majors))
          mngFields.put(mngProject.id + "_directions", CollectUtils.intersection(entityDao.get(classOf[Direction], 
            "major.project.id", mngProject.id), directions))
        }
      }
    }
    val userId = getLong("user.id")
    val aoProfiles = entityDao.get(classOf[User], userId).getProfiles
    val projectId2aoProfile = CollectUtils.newHashMap()
    for (aoProfile <- aoProfiles) {
      val projects = profileService.getProperty(aoProfile, projectField).asInstanceOf[List[_]]
      val aoProject = projects.get(0).asInstanceOf[Project]
      projectId2aoProfile.put(aoProject.id.toString, aoProfile)
      val profileId = PropertyUtils.getProperty(aoProfile, "id")
      aoFields.put(profileId + "_project", aoProject)
      aoFields.put(profileId + "_educations", profileService.getProperty(aoProfile, educationField))
      aoFields.put(profileId + "_stdTypes", profileService.getProperty(aoProfile, stdTypeField))
      aoFields.put(profileId + "_departs", profileService.getProperty(aoProfile, departField))
      aoFields.put(profileId + "_majors", profileService.getProperty(aoProfile, majorField))
      aoFields.put(profileId + "_directions", profileService.getProperty(aoProfile, directionField))
    }
    put("mngProjects", mngProjects)
    put("mngFields", mngFields)
    put("projectId2aoProfile", projectId2aoProfile)
    put("aoProfiles", aoProfiles)
    put("aoFields", aoFields)
    var allProfileDeparts = getMyProfileValues(mngProfiles, departField).asInstanceOf[List[_]]
    var allProfileProjects = getMyProfileValues(mngProfiles, projectField).asInstanceOf[List[_]]
    if (CollectUtils.isEmpty(allProfileDeparts) && isAdmin) {
      allProfileDeparts = entityDao.getAll(classOf[Department])
    }
    if (CollectUtils.isEmpty(allProfileProjects)) {
      allProfileProjects = entityDao.getAll(classOf[Project])
    }
    val projId_departId2Majors = CollectUtils.newHashMap()
    val projId_departId_Majors = entityDao.search(OqlBuilder.from(classOf[MajorJournal].getName + " md")
      .select("md.major.project.id, md.depart.id, md.major")
      .where("md.depart in (:departs)", allProfileDeparts)
      .where("md.major.project in (:projects)", allProfileProjects)
      .orderBy("md.major.project.id, md.depart.id, md.major.code")).asInstanceOf[List[_]]
    for (projId_departId_Major <- projId_departId_Majors) {
      val projectId = projId_departId_Major(0).asInstanceOf[java.lang.Integer]
      val departId = projId_departId_Major(1).asInstanceOf[java.lang.Integer]
      val major = projId_departId_Major(2).asInstanceOf[Major]
      val key = "p" + projectId + "_" + departId
      var majors = projId_departId2Majors.get(key)
      if (majors == null) {
        majors = new TreeSet[Major](new Comparator[Major]() {

          def compare(arg0: Major, arg1: Major): Int = {
            return arg0.getCode.compareTo(arg1.getCode)
          }
        })
        projId_departId2Majors.put(key, majors)
      }
      majors.add(major)
    }
    put("projId_departId2Majors", projId_departId2Majors)
    val projId_departId_majorId2directions = CollectUtils.newHashMap()
    val projId_departId_majorId_directions = entityDao.search(OqlBuilder.from(classOf[DirectionJournal].getName + " md")
      .select("md.direction.major.project.id, md.depart.id, md.direction.major.id, md.direction")
      .where("md.depart in (:departs)", allProfileDeparts)
      .where("md.direction.major.project in (:projects)", allProfileProjects)
      .orderBy("md.direction.major.project.id, md.direction.major.id, md.depart.id, md.direction.code")).asInstanceOf[List[_]]
    for (projId_departId_majorId_direction <- projId_departId_majorId_directions) {
      val projectId = projId_departId_majorId_direction(0).asInstanceOf[java.lang.Integer]
      val departId = projId_departId_majorId_direction(1).asInstanceOf[java.lang.Integer]
      val majorId = projId_departId_majorId_direction(2).asInstanceOf[java.lang.Integer]
      val direction = projId_departId_majorId_direction(3).asInstanceOf[Direction]
      val key = "p" + projectId + "_" + departId + "_" + majorId
      var directions = projId_departId_majorId2directions.get(key)
      if (directions == null) {
        directions = new TreeSet[Direction](new Comparator[Direction]() {

          def compare(arg0: Direction, arg1: Direction): Int = {
            return arg0.getCode.compareTo(arg1.getCode)
          }
        })
        projId_departId_majorId2directions.put(key, directions)
      }
      directions.add(direction)
    }
    put("projId_departId_majorId2directions", projId_departId_majorId2directions)
    forward()
  }

  def saveConfig(): String = {
    val userId = getLong("user.id")
    var projectIds = Strings.transformToLong(getAll("projects").asInstanceOf[Array[String]])
    if (projectIds == null) {
      projectIds = Array.ofDim[Long](0)
    }
    val visibleProfileIds = Strings.transformToLong(getAll("profile.id").asInstanceOf[Array[String]])
    val profiles = entityDao.get(classOf[User], userId).getProfiles
    val _fs = extractFields()
    val projectField = _fs(0)
    val educationField = _fs(1)
    val stdTypeField = _fs(2)
    val departField = _fs(3)
    val majorField = _fs(4)
    val directionField = _fs(5)
    val saveEntities = CollectUtils.newArrayList()
    val removeEntitties = CollectUtils.newArrayList()
    for (projectId <- projectIds) {
      val profile = extractProfileByProject(projectField, profiles, projectId)
      profiles.remove(profile)
      if (null != PropertyUtils.getProperty(profile, "id")) {
        profile.getProperties.clear()
      }
      setProfileProperty(profile, projectField, Array(projectId.toString))
      val educations = getAll("p" + projectId + "_educations").asInstanceOf[Array[String]]
      setProfileProperty(profile, educationField, educations)
      val stdTypes = getAll("p" + projectId + "_stdTypes").asInstanceOf[Array[String]]
      setProfileProperty(profile, stdTypeField, stdTypes)
      val departs = getAll("p" + projectId + "_departs").asInstanceOf[Array[String]]
      setProfileProperty(profile, departField, departs)
      val majors = getAll("p" + projectId + "_majors").asInstanceOf[Array[String]]
      setProfileProperty(profile, majorField, majors)
      val directions = getAll("p" + projectId + "_directions").asInstanceOf[Array[String]]
      if (!Arrays.isEmpty(educations) && !Arrays.isEmpty(stdTypes) && 
        !Arrays.isEmpty(departs) && 
        !Arrays.isEmpty(majors) && 
        !Arrays.isEmpty(directions)) {
        profile.setProperty(directionField, Property.AllValue)
      } else {
        setProfileProperty(profile, directionField, directions)
      }
      saveEntities.add(profile)
    }
    for (profile <- profiles) {
      var isAbandoned = false
      for (profileId <- visibleProfileIds if profileId == PropertyUtils.getProperty(profile, "id")) {
        isAbandoned = true
        //break
      }
      if (isAbandoned) {
        removeEntitties.add(profile)
      }
    }
    entityDao.execute(new Builder().remove(removeEntitties).saveOrUpdate(saveEntities))
    redirect("info", "info.save.success")
  }

  private def getMyProfileValues[T](Profiles: List[Profile], field: Field): List[T] = {
    val values = CollectUtils.newArrayList()
    val profileService = securityHelper.getProfileService
    for (profile <- Profiles) {
      val property = profile.getProperty(field)
      if (null != property) {
        val value = property.getValue
        if (null != value) {
          if (property.getField.isMultiple) {
            values.addAll(profileService.getProperty(profile, field).asInstanceOf[Iterable[_]])
          } else {
            values.add(profileService.getProperty(profile, field))
          }
        }
      }
    }
    values.asInstanceOf[List[T]]
  }

  private def extractFields(): Array[Field] = {
    val fields = entityDao.getAll(classOf[Field])
    val res = Array.ofDim[Field](6)
    for (field <- fields) {
      if ("projects" == field.getName) {
        res(0) = field
      } else if ("educations" == field.getName) {
        res(1) = field
      } else if ("stdTypes" == field.getName) {
        res(2) = field
      } else if ("departs" == field.getName) {
        res(3) = field
      } else if ("majors" == field.getName) {
        res(4) = field
      } else if ("directions" == field.getName) {
        res(5) = field
      }
    }
    res
  }

  private def extractProfileByProject(projectField: Field, profiles: List[Profile], projectId: java.lang.Long): Profile = {
    var profile: Profile = null
    for (uprofile <- profiles) {
      val projects = securityHelper.getProfileService.getProperty(uprofile, projectField).asInstanceOf[List[_]]
      var suitable = false
      for (project <- projects if project.id == projectId) {
        suitable = true
        //break
      }
      if (suitable) {
        profile = uprofile
        //break
      }
    }
    if (profile == null) {
      profile = new UserProfileBean()
      profile.asInstanceOf[UserProfileBean].setUser(new UserBean(getLong("user.id")))
    }
    profile
  }

  private def setProfileProperty(profile: Profile, field: Field, values: Array[String]) {
    if (null == values || values.length == 0) {
      profile.setProperty(field, Property.AllValue)
    } else {
      var storedValue: String = null
      if (null != field.getKeyName) {
        val keys = CollectUtils.newHashSet(values)
        var allValues = securityHelper.getProfileService.getFieldValues(field)
        allValues = CollectUtils.select(allValues, new Predicate() {

          def apply(arg0: AnyRef): java.lang.Boolean = {
            try {
              var keyValue = String.valueOf(PropertyUtils.getProperty(arg0, field.getKeyName))
              return keys.contains(keyValue)
            } catch {
              case e: Exception => e.printStackTrace()
            }
            return false
          }
        })
        storedValue = identifierDataResolver.marshal(field, allValues)
      } else {
        storedValue = Strings.join(values)
      }
      profile.setProperty(field, storedValue)
    }
  }

  def info(): String = {
    val aoProfiles = entityDao.get(classOf[User], getLong("user.id")).getProfiles
    var mngProjects = CollectUtils.newArrayList()
    val _fs = extractFields()
    val projectField = _fs(0)
    if (isAdmin) {
      mngProjects = entityDao.getAll(classOf[Project])
    } else {
      val mngProfiles = entityDao.get(classOf[User], getUserId).getProfiles
      for (mngProfile <- mngProfiles) {
        mngProjects.addAll(getMyProfileValues(Collections.singletonList(mngProfile), projectField).asInstanceOf[List[_]])
      }
    }
    val visibleProfile = CollectUtils.newArrayList()
    for (aoProfile <- aoProfiles) {
      val aoProjects = getMyProfileValues(Collections.singletonList(aoProfile.asInstanceOf[Profile]), 
        projectField).asInstanceOf[List[_]]
      for (mngProject <- mngProjects if CollectUtils.isNotEmpty(aoProjects) && aoProjects.get(0).id == mngProject.id) {
        visibleProfile.add(aoProfile)
        //break
      }
    }
    new ProfileHelper(entityDao, securityHelper.getProfileService)
      .populateInfo(visibleProfile)
    forward()
  }
}
