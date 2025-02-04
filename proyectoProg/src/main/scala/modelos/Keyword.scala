package modelos
import play.api.libs.json.{Json, Reads}

case class Keyword(
                    id: Int,
                    name: String
                  )
object Keyword {
  implicit val keywordReads: Reads[Keyword] = Json.reads[Keyword]
}
