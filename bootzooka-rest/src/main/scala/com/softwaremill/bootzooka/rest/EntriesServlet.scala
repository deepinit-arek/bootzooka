package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.common.{ SafeLong, NotEscapedJsonWrapper, JsonWrapper }
import com.softwaremill.bootzooka.service.entry.EntryService

class EntriesServlet(entryService: EntryService, val userService: UserService) extends JsonServletWithAuthentication {

  get("/:id") {
    entryService.load(params("id")) match {
      case Some(e) => NotEscapedJsonWrapper(e)
      case _ =>
    }
  }

  get("/") {
    entryService.loadAll
  }

  get("/count") {
    JsonWrapper(entryService.count())
  }

  get("/count-newer/:time") {
    val longOpt = SafeLong(params("time"))

    longOpt match {
      case Some(t) => JsonWrapper(entryService.countNewerThan(t))
      case _ => JsonWrapper(0)
    }
  }

  // create new entry
  post("/") {
    haltIfNotAuthenticated()
    val entryText = (parsedBody \ "text").extract[String]
    entryService.add(user.login, entryText)
  }

  // update existing entry
  put("/") {
    haltIfNotAuthenticated()
    val text: String = (parsedBody \ "text").extract[String]
    val id: String = (parsedBody \ "id").extract[String]

    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.update(id, text)
  }

  delete("/:id") {
    haltIfNotAuthenticated()

    val id: String = params("id")
    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.remove(id)
  }

}
