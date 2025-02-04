package modelos
import play.api.libs.json.{Json, Reads}

case class Crew(
                 credit_id: String,
                 department: String,
                 gender: Int,
                 id: Int,
                 job: String,
                 name: String,
                 profile_path: String
               )

object Crew {
  implicit val CrewReads: Reads[Crew] = Json.reads[Crew]
}
