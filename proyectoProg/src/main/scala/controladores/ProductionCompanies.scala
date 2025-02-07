package controladores

import utils.LimpiezaJSON.limpiarJson
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import modelos.ProductionCompany
import play.api.libs.json.{JsArray, JsError, JsSuccess, Json}
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.Try
import INSERTS.*
object ProductionCompanies extends App {
  val rutaCsv = "data/pi_movies_complete.csv"

  implicit val csvFormat: DefaultCSVFormat = new DefaultCSVFormat {
    override val delimiter = ';'
  }

  val reader = CSVReader.open(new File(rutaCsv))

  try {
    val datos = reader.allWithHeaders()

    val datosFiltrados = datos.filter { fila =>
      val columnasExcluyendoId = fila - "id"
      columnasExcluyendoId.values.exists(_.trim.nonEmpty) &&
        !columnasExcluyendoId.values.forall(_.trim.toLowerCase == "<unset>")
    }.distinct

    println(s"Total de filas después de filtrar: ${datosFiltrados.size}")

    val companiesList = new ListBuffer[ProductionCompany]()
    val movieCompaniesList = new ListBuffer[(Int, Int, String)]()

    datosFiltrados.foreach { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("production_companies", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")
        jsonArray <- Try(Json.parse(jsonLimpio).as[JsArray]).toOption
      } jsonArray.value.foreach { obj =>
        obj.validate[ProductionCompany] match {
          case JsSuccess(company, _) =>
            companiesList += company
            movieCompaniesList += ((movieId, company.id, company.name))
          case JsError(errors) =>
            println(s"Error en el JSON de production_companies: $errors")
        }
      }
    }
    if (INSERTPRODUCTIONCOMPANIES.InsertIntoProductionCompanies(movieCompaniesList.toList))
      println("CORRECTO")
    else
      println("ERROR")
    
  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
