package org.openurp.edu.eams.teach.schedule.json

import org.openurp.edu.eams.base.model.RoomBean
import org.openurp.edu.eams.base.model.json.RoomBeanGsonAdapter
import org.openurp.edu.eams.teach.lesson.model.ArrangeSuggestBean
import org.openurp.edu.eams.teach.lesson.model.SuggestActivityBean
import com.google.gson.Gson
import com.google.gson.GsonBuilder



object ArrangeSuggestGsonBuilder {

  def build(): Gson = {
    new GsonBuilder().registerTypeAdapter(classOf[ArrangeSuggestBean], new ArrangeSuggestGsonAdapter())
      .registerTypeAdapter(classOf[SuggestActivityBean], new SuggestActivityGsonAdapter())
      .registerTypeAdapter(classOf[RoomBean], new RoomBeanGsonAdapter())
      .create()
  }
}
