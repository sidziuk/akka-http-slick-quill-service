package com.sidziuk

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server._
import spray.json._

class RestApi(service: CarDatabaseService) {

  import Directives._

  private object JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val colorManufacturer: RootJsonFormat[Manufacturer] = jsonFormat2(Manufacturer)
    implicit val matchingDTOFormat: RootJsonFormat[MatchingDTO] = jsonFormat2(MatchingDTO)
    implicit val carFormat: RootJsonFormat[Car] = jsonFormat3(Car)
  }

  import JsonProtocol._

  val route: Route = pathPrefix("api") {
    pathEndOrSingleSlash {
      get {
        complete("This is the OpenBean homework")
      }
    } ~ pathPrefix("cars") {
      pathEndOrSingleSlash {
        get {
          complete {
            service.all()
          }
        } ~ post {
          entity(as[Car]) { newCar =>
            complete {
              service.add(newCar)
            }
          }
        }
      } ~ path("matching") {
        post {
          entity(as[MatchingDTO]) { matchingDTO  =>
            complete {
              service.matching(matchingDTO.manufacturer, matchingDTO.color)
            }
          }
        }
      } ~ path(Segment) { licensePlateStr =>
        get {
          complete {
            service.byPlate(licensePlateStr)
          }
        }
      }
    }
  }
}
