package modelos
import play.api.libs.json.{Json, Reads}

case class ProductionCountry(
                    iso_3166_1: String,
                    name: String
                  )
object ProductionCountry {
  implicit val ProductionCountryReads: Reads[ProductionCountry] = Json.reads[ProductionCountry]
}