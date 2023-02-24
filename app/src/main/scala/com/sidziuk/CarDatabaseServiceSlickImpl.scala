package com.sidziuk

import akka.Done
import com.sidziuk.Runner.{cars, manufacturers, profile}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class CarDatabaseServiceSlickImpl(database: profile.backend.DatabaseDef)(implicit ec: ExecutionContext) extends CarDatabaseService with StrictLogging {

  import profile.api._

  override def add(car: Car): Future[Done] = database
    .run {
      for {
        _ <- cars += car
          .toCarRow
        _ <- manufacturers += car
          .manufacturer
      } yield Done
    }.fallbackTo(Future(Done))

  override def all(): Future[Set[Car]] = {
    val query = cars
      .join(manufacturers)
      .on(_.manufacturer === _.name)

    database
      .run {
        query
          .result
      }
      .map(_.toSet)
      .map {
        _.
          map { case (carRow, manufacturer) =>
            carRow
              .toCar(manufacturer)
          }
      }
  }

  override def byPlate(plate: LicensePlate): Future[Option[Car]] = {
    val query = cars
      .filter(car => car.licensePlate === plate)
      .join(manufacturers)
      .on(_.manufacturer === _.name)

    database
      .run {
        query
          .result
      }
      .map(_.headOption)
      .map {
        _.
          map { case (carRow, manufacturer) =>
            carRow
              .toCar(manufacturer)
          }
      }
  }

  override def matching(manufacturer: Manufacturer, color: Color): Future[Set[Car]] = {

    val query = cars
      .filter(car => car.color === color && car.manufacturer === manufacturer.name)
      .join(manufacturers)
      .on(_.manufacturer === _.name)

    database
      .run {
        query
          .result
      }
      .map(_.toSet)
      .map {
        _.
          map { case (carRow, manufacturer) =>
            carRow
              .toCar(manufacturer)
          }
      }
  }
}