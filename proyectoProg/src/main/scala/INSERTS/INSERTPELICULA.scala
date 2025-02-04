package INSERTS

import com.github.tototoshi.csv.*
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.util.Try
import java.io.{BufferedWriter, FileWriter}
import java.io.File
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}
import play.api.libs.json.*
import utils._

object INSERTPELICULA extends App {
  implicit object CSVFormatter extends DefaultCSVFormat {
    override val delimiter: Char = ';'
  }
  val rutaCsv = "data/pi_movies_complete.csv"
  val pathSQLPelicula = "src/main/INSERT_PELICULA.sql"

  val reader = CSVReader.open(new File(rutaCsv))
  val datos = reader.allWithHeaders()
  
  
  val dataPelicula = converPeliculaData(datos.distinct)
      println(dataPelicula.size)

      if(InsertIntoPelicula(dataPelicula,pathSQLPelicula))
        println("CON EXTITO")
      else
        println("fallo")
}
def converPeliculaData(data: List[Map[String, String]]): List[
  (String, Long, String, Long, String, String, String, String, Double, String, String, Long, Int, String, String, String, String, Double, Int)
] = {
  data.map { row =>
    (
      row.get("adult").filter(_.nonEmpty).getOrElse("").trim, // Strings: Asignar "" si es None o está vacío
      row.get("budget").flatMap(s => Try(s.toLong).toOption).getOrElse(0L), // Long: Asignar 0 si es None o no es un número válido
      row.get("homepage").filter(_.nonEmpty).getOrElse("").trim,
      row.get("id").flatMap(s => Try(s.toLong).toOption).getOrElse(0L),
      row.get("imdb_id").filter(_.nonEmpty).getOrElse("").trim,
      row.get("original_language").filter(_.nonEmpty).getOrElse("").trim,
      row.get("original_title").filter(_.nonEmpty).getOrElse("").trim,
      row.get("overview").filter(_.nonEmpty).getOrElse("").trim,
      row.get("popularity").flatMap(s => Try(s.toDouble).toOption).getOrElse(0.0), // Double: Asignar 0.0 si es None o no es un número válido
      row.get("poster_path").filter(_.nonEmpty).getOrElse("").trim,
      row.get("release_date").filter(_.nonEmpty).getOrElse("").trim,
      row.get("revenue").flatMap(s => Try(s.toLong).toOption).getOrElse(0L),
      row.get("runtime").flatMap(s => Try(s.toInt).toOption).getOrElse(0), // Int: Asignar 0 si es None o no es un número válido
      row.get("status").filter(_.nonEmpty).getOrElse("").trim,
      row.get("tagline").filter(_.nonEmpty).getOrElse("").trim,
      row.get("title").filter(_.nonEmpty).getOrElse("").trim,
      row.get("video").filter(_.nonEmpty).getOrElse("").trim,
      row.get("vote_average").flatMap(s => Try(s.toDouble).toOption).getOrElse(0.0),
      row.get("vote_count").flatMap(s => Try(s.toInt).toOption).getOrElse(0)
    )
  }
}


def InsertIntoPelicula(data: List[(String, Long, String, Long, String, String, String, String, Double, String, String, Long, Int, String, String, String, String, Double, Int)], path: String): Boolean =
  def generateINSERT(row: (String, Long, String, Long, String, String, String, String, Double, String, String, Long, Int, String, String, String, String, Double, Int)): String =
    s"INSERT INTO PELICULA(adult, budget, homepage, id, imdb_id, original_language, original_title, overview, popularity, poster_path, release_date, revenue, runtime, status, tagline, title, video, vote_average, vote_count) VALUES('${LimpiezaGeneral.escapeMySQLString(row._1)}',${row._2},'${LimpiezaGeneral.escapeMySQLString(row._3)}',${row._4},'${LimpiezaGeneral.escapeMySQLString(row._5)}','${LimpiezaGeneral.escapeMySQLString(row._6)}','${LimpiezaGeneral.escapeMySQLString(row._7)}','${LimpiezaGeneral.escapeMySQLString(row._8)}',${row._9},'${LimpiezaGeneral.escapeMySQLString(row._10)}','${LimpiezaGeneral.escapeMySQLString(row._11)}',${row._12},${row._13},'${LimpiezaGeneral.escapeMySQLString(row._14)}','${LimpiezaGeneral.escapeMySQLString(row._15)}','${LimpiezaGeneral.escapeMySQLString(row._16)}','${LimpiezaGeneral.escapeMySQLString(row._17)}',${row._18},${row._19});"

  try {
    val file = new BufferedWriter(new FileWriter(path))
    data.foreach { row =>
      file.write(generateINSERT(row))
      file.newLine()
    }
    file.close()
    true
  } catch {
    case e: Exception =>
      println(e.getMessage)
      false
  }