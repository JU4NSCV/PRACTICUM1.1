package controladores

import utils.LimpiezaJSON.{limpiarJson, limpiarJsonCrew}
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import play.api.libs.json.{JsObject, Json}
import java.io.{File, PrintWriter}
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.{ListBuffer, Map}
import INSERTS.*
object Crew extends App {
  val rutaCsv = "data/pi_movies_complete.csv"

  implicit val csvFormat: DefaultCSVFormat = new DefaultCSVFormat {
    override val delimiter = ';'
  }

  val reader = CSVReader.open(new File(rutaCsv))

  try {
    val datos = reader.allWithHeaders()

    val datosFiltrados = datos
      .filter { fila =>
        val columnasExcluyendoId = fila - "id"
        columnasExcluyendoId.values.exists(_.trim.nonEmpty) &&
          !columnasExcluyendoId.values.forall(valor => valor.trim.toLowerCase == "<unset>")
      }
      .distinct

    println(s"Total de filas después de filtrar: ${datosFiltrados.size}")

    val personnelList = new ListBuffer[Map[String, Any]]()
    val moviePersonnelList = new ListBuffer[(Int, Int, Int)]()
    val jobList = new ListBuffer[String]()
    val departmentList = new ListBuffer[String]()
    val personnelJobList = new ListBuffer[(Int, Int, Int, Int, String, String,String,String)]()
    val jobDepartmentMap = Map[String, String]()

    datosFiltrados.foreach { fila =>
      val movieIdStr = fila.getOrElse("id", "").trim
      val jsonStr = fila.getOrElse("crew", "").trim

      if (jsonStr.nonEmpty && jsonStr != "\"\"") {
        val movieIdOpt = Try(movieIdStr.toInt).toOption

        movieIdOpt.foreach { movieId =>
          val jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")

          Try(Json.parse(jsonLimpio).as[List[JsObject]]) match {
            case Success(jsonArray) =>
              jsonArray.foreach { jsonObj =>
                val creditId = (jsonObj \ "credit_id").asOpt[Int].getOrElse(-1)
                val department = (jsonObj \ "department").asOpt[String].filter(_.nonEmpty).getOrElse("null")
                val gender = (jsonObj \ "gender").asOpt[Int].getOrElse(-1)
                val idPersonnel = (jsonObj \ "id").asOpt[Int].getOrElse(-1)
                val job = (jsonObj \ "job").asOpt[String].filter(_.nonEmpty).getOrElse("null")
                val name = (jsonObj \ "name").asOpt[String].filter(_.nonEmpty).getOrElse("null")
                val profilePath = (jsonObj \ "profile_path").asOpt[String].getOrElse("null")

                personnelList += Map(
                  "id_personnel" -> idPersonnel,
                  "name" -> name,
                  "gender" -> gender,
                  "profile_path" -> profilePath
                )
                moviePersonnelList += ((movieId, idPersonnel, creditId))

                if (!jobList.contains(job)) jobList += job
                if (!departmentList.contains(department)) departmentList += department
                jobDepartmentMap(job) = department

                personnelJobList += ((idPersonnel, jobList.indexOf(job) + 1, creditId,gender,department,job,profilePath,name))
              }
            case Failure(exception) =>
              println(s"Error al parsear JSON de crew para Movie ID: $movieId: ${exception.getMessage}\nJSON: $jsonLimpio")
          }
        }
      }
    }

    if (INSERTCREW.InsertIntoBelongToCollection(personnelJobList.toList))
      println("CORRECTO")
    else
      println("ERROR")
      
    // Aquí podrías seguir procesando los datos como desees sin insertar en SQL, como por ejemplo, imprimir los resultados o guardarlos en un archivo

    println("Datos procesados:")
    println(s"Total de personas: ${personnelList.size}")
    println(s"Total de relaciones de película-personal: ${moviePersonnelList.size}")
    println(s"Total de trabajos: ${jobList.size}")
    println(s"Total de departamentos: ${departmentList.size}")
    // O también podrías guardar los datos en otro formato, como JSON, CSV, etc.

  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
