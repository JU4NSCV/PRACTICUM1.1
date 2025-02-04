package modelos

import play.api.libs.json._

case class BelongsToCollection(

                                id: Int,
                                name: String,
                                posterPath: String,
                                backdropPath: String
                              )


object BelongsToCollection {
  implicit val BelongsToCollectionReads: Reads[BelongsToCollection] = Json.reads[BelongsToCollection]
}
