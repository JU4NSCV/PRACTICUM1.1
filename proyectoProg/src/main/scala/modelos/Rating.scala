package modelos

import play.api.libs.json._

case class Rating(
                   userId: Int,
                   rating: Double,
                   timestamp: Long
                 )

// Implementar `Reads` para Play JSON
object Rating {
  implicit val RatingReads: Reads[Rating] = Json.reads[Rating]
}