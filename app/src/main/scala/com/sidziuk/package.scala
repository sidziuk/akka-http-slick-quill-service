package com

package object sidziuk {

  type LicensePlate = String
  type Color = String

  final case class Car(licensePlate: LicensePlate, manufacturer: Manufacturer, color: Color){
    def toCarRow: CarRow = CarRow(
      licensePlate = licensePlate,
      manufacturer = manufacturer.name,
      color = color)
  }
  final case class CarRow(licensePlate: LicensePlate, manufacturer: String, color: Color) {
    def toCar(manufacturer: Manufacturer): Car = Car(
      licensePlate = licensePlate,
      manufacturer = manufacturer,
      color = color)
  }
  final case class Manufacturer(name: String, country: String)

  final case class MatchingDTO(manufacturer: Manufacturer, color: Color)
}
