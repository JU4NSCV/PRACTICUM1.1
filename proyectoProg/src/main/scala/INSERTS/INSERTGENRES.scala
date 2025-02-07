package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTGENRES extends App {
  val pathSQLgenres = "src/main/INSERT_GENRES.sql"

  def InsertIntoGenres(data: List[(Int, Int, String)]): Boolean =
    def generateINSERT(row: (Int, Int, String)): String =
      s"insert into Genres (id, id_movie, name) VALUE  (${row._1},${row._2},'${row._3}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLgenres))
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
}
