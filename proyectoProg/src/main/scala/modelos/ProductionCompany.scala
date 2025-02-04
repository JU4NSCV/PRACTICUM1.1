package modelos

import play.api.libs.json._

case class ProductionCompany(
                     id: Int,
                     name: String
                   )

object ProductionCompany {
  implicit val ProductionCompanyReads: Reads[ProductionCompany] = Json.reads[ProductionCompany]
}
