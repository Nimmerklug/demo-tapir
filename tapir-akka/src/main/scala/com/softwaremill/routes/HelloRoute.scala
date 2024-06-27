package com.softwaremill.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object HelloRoute {
  val routes: Route = {
    get {
      concat(
        path("hello") {
          complete("Hello World!")
        },
        path("hello-name") {
          parameters(Symbol("name").as[String]) { name =>
            {
              complete(s"Hello $name")
            }
          }
        },
        path("hello-user") {
          headerValueByName("X-User-Id") { userId =>
            if (userId == "123") {
              complete("Welcome user")
            } else {
              complete(StatusCodes.Unauthorized, "Incorrect user")
            }
          }
        }
      )
    } ~
      post {
        path("hello-name") {
          entity(as[String]) { name =>
            complete(s"Hello $name")
          }
        }
      }
  }
}
