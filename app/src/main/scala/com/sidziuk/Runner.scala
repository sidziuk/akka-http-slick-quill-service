package com.sidziuk

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{MysqlJAsyncContext, SnakeCase}

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Runner extends App with StrictLogging with dbProfile with Tables {

  import profile.api._

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val ac: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(ac)

  val interface = "127.0.0.1"
  val port = 1234

  logger.info("Start database initialization")
  val initDatabase: profile.backend.DatabaseDef = profile.api.Database.forConfig("slick.init.db")
  Await.ready(initDatabase.run(sqlu"create database if not exists test;"), 20.seconds)
  initDatabase.close()
  val database: profile.backend.DatabaseDef = profile.api.Database.forConfig("slick.db")
  val oo = DBIO.seq(
    manufacturers.schema.createIfNotExists,
    cars.schema.createIfNotExists)
  Await.ready(database.run(oo), 20.seconds)
  val insertActions = DBIO.seq(
    manufacturers ++= Seq(
      Manufacturer("Skoda", "Czech Republic"),
      Manufacturer("Toyota", "Japan"),
    ),
    cars ++= Seq(
      CarRow("AB12", "Skoda", "#B22222"),
      CarRow("CD78", "Skoda", "#FF8C00"),
      CarRow("NJ45", "Toyota", "#191970")
    )
  )
  database.run(insertActions)
  logger.info("Database was initialized")

  val ecDBS: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val carDatabaseServiceSlick = new CarDatabaseServiceSlickImpl(database)(ecDBS)

  lazy val ctx = new MysqlJAsyncContext(SnakeCase, "ctx")
  val carDatabaseServiceQuill = new CarDatabaseServiceQuillImpl(ctx)(ecDBS)

  val api = new RestApi(carDatabaseServiceQuill)

  Http().newServerAt(interface, port).bind(api.route) onComplete {
    case Success(binding) =>
      val addr = binding.localAddress.getAddress.getCanonicalHostName + ":" + binding.localAddress.getPort
      logger.info(s"HTTP API listening on $addr, try http://$addr/api")
    case Failure(t) =>
      logger.error(s"Failed to bind HTTP API", t);
      sys.exit(-1)
  }
}
