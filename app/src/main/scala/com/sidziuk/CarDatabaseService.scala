package com.sidziuk

import akka.Done

import scala.concurrent.Future

trait CarDatabaseService {

  def add(car: Car): Future[Done]

  def all(): Future[Set[Car]]

  def byPlate(plate: LicensePlate): Future[Option[Car]]

  def matching(manufacturer: Manufacturer, color: Color): Future[Set[Car]]

}
