package com.example.catseffectbug

import cats.effect.{Effect, IO}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.log4s.{getLogger, Logger}

object Routes {

  val log: Logger = getLogger

  def helloWorldRoutes(H: HelloWorld): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO]{}
    import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" / name =>
        val response = for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp

        for {
        resp <- Effect[IO].handleErrorWith(response)(error => {
          IO(log.error(s"Encountered error..... $error")) *> IO.raiseError(error)
        })
        } yield resp
    }
  }
}