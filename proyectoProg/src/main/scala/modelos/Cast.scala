package modelos
import play.api.libs.json._

case class Cast(
                 cast_id: Int,
                 character: String,
                 credit_id: String,
                 gender: Option[Int],
                 id: Int,
                 name: String,
                 order: Int,
                 profile_path: Option[String]
               )

object Cast {
  implicit val CastReads: Reads[Cast] = Json.reads[Cast]
}