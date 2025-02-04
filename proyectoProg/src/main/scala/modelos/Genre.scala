package modelos

import play.api.libs.json._

case class Genre(
                  id: Int,
                  name: String
                )

// Implementar Reads para Play JSON
object Genre {
  implicit val GenreReads: Reads[Genre] = Json.reads[Genre]
}
