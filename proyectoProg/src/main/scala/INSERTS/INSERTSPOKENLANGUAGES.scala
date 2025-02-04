package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTSPOKENLANGUAGES {
  val pathSQLSpokenLanguages = "src/main/INSERT_SPOKEN_LANGUAGES.sql"

  def InsertIntoBelongToCollection(data: List[(Int, String, String)]): Boolean =
    def generateINSERT(row: (Int, String, String)): String =
      s"INSERT INTO spoken_languages (id_movie, iso_639_1, name) VALUE (${row._1},'${row._2}','${row._3}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLSpokenLanguages))
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
