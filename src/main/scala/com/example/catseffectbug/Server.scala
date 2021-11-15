package com.example.catseffectbug

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.log4s.getLogger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

object Server extends IOApp {

  private val log = getLogger

  import cats.effect.{ContextShift, Timer}
  override implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  override implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def run(args: List[String]): IO[ExitCode] = {
        for {
          exitCode <- mountHttpService.guarantee(shutdownApp)
        } yield exitCode
  }

  def shutdownApp: IO[Unit] =
    shutdownApp(5 seconds)

  def shutdownApp(time: FiniteDuration): IO[Unit] =
    for {
        _     <-  IO(log.info(s"Shutting down app in $time"))
        _     <-  IO.sleep(time)
        _     <-  IO(log.info("Goodbye!"))
    } yield ()


  def mountHttpService: IO[ExitCode] = {
    val helloWorldAlg = HelloWorld.impl

    val httpApp = Routes.helloWorldRoutes(helloWorldAlg).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
