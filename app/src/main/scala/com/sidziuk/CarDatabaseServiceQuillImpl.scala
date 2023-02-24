package com.sidziuk

import io.getquill._
import akka.Done
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.{ExecutionContext, Future}

class CarDatabaseServiceQuillImpl(ctx: MysqlJAsyncContext[SnakeCase.type])(implicit ec: ExecutionContext) extends CarDatabaseService with StrictLogging {

  import ctx._

  val manufacturerRow = quote {
    querySchema[Manufacturer](
      "manufacturer",
      _.name -> "name",
      _.country -> "country")
  }
  val carRow = quote {
    querySchema[CarRow](
      "car",
      _.licensePlate -> "license_plate",
      _.manufacturer -> "manufacturer_name",
      _.color -> "color")
  }

  override def add(car: Car): Future[Done] = {

    val requestCar = quote(carRow.insertValue(lift(car.toCarRow)))
    val requestManufacturer = quote(manufacturerRow.insertValue(lift(car.manufacturer)).onConflictIgnore)

    for {
      _ <- ctx.run(requestCar).fallbackTo(Future(Done))
      _ <- ctx.run(requestManufacturer)
    } yield Done

  }

  override def all(): Future[Set[Car]] = {
    val request = quote(
      for {
        car <- carRow
        manufacturer <- manufacturerRow.join(_.name == car.manufacturer)
      } yield (car, manufacturer)
    )
    ctx
      .run(request)
      .map { rows =>
        rows
          .map { case (car, manufacturer) => car
            .toCar(manufacturer)
          }
          .toSet
      }
  }

  override def byPlate(plate: LicensePlate): Future[Option[Car]] = {
    val request = quote(
      for {
        car <- carRow.filter(_.licensePlate == lift(plate))
        manufacturer <- manufacturerRow.join(_.name == car.manufacturer)
      } yield (car, manufacturer)
    )
    ctx
      .run(request)
      .map { rows =>
        rows
          .map { case (car, manufacturer) => car
            .toCar(manufacturer)
          }
          .headOption
      }
  }

  override def matching(manufacturer: Manufacturer, color: Color): Future[Set[Car]] = {
    val request = quote(
      for {
        car <- carRow.filter(car => car.color == lift(color) && car.manufacturer == lift(manufacturer.name))
        manufacturer <- manufacturerRow.join(_.name == car.manufacturer)
      } yield (car, manufacturer)
    )
    ctx
      .run(request)
      .map { rows =>
        rows
          .map { case (car, manufacturer) => car
            .toCar(manufacturer)
          }
          .toSet
      }
  }
}