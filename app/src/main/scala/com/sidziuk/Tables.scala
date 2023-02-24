package com.sidziuk

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait dbProfile {
  lazy val profile: JdbcProfile = DatabaseConfig.forConfig[JdbcProfile]("slick").profile
}
trait Tables {

  val profile: JdbcProfile

  import profile.api._

  class ManufacturerTable(tag: Tag) extends Table[Manufacturer](tag, "manufacturer") {
    def name = column[String]("name", O.PrimaryKey)

    def country = column[String]("country")

    def * = (name, country) <> ((Manufacturer.apply _).tupled, Manufacturer.unapply)
  }

  val manufacturers = TableQuery[ManufacturerTable]

//  final case class CarRow(licensePlate: LicensePlate, manufacturer: String, color: Option[Color])

  class CarTable(tag: Tag) extends Table[CarRow](tag, "car") {
    def licensePlate = column[LicensePlate]("license_plate", O.PrimaryKey)

    def manufacturer = column[String]("manufacturer_name")

    def color = column[Color]("color")

    def * = (licensePlate, manufacturer, color) <> ((CarRow.apply _).tupled, CarRow.unapply)

    // A reified foreign key relation that can be navigated to create a join
//    def supplier = foreignKey("m_fk", manufacturer, manufacturers)(_.name)
  }

  val cars = TableQuery[CarTable]

}


