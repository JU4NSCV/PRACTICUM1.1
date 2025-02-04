package modelos
import play.api.libs.json.{Json, Reads}

case class SpokenLanguage(
                             iso_639_1: String,
                             name: String
                           )
object SpokenLanguage {
  implicit val SpokenLanguageReads: Reads[SpokenLanguage] = Json.reads[SpokenLanguage]
}