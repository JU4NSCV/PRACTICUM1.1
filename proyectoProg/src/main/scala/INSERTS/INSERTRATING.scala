package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTRATING {
  val pathSQLRating = "src/main/INSERT_RATING.sql"

  def InsertIntoRating(data: List[(Int, Int, Double, Long)]): Boolean =
    def generateINSERT(row: (Int, Int, Double, Long)): String =
      s"INSERT INTO ratings (id_movie,userId, rating,_timestamp) VALUE  (${ row._1 }, ${ row._2 }, ${ row._3 }, ${ row._4 });"
      
    try {
      val file = new BufferedWriter(new FileWriter(pathSQLRating))
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
