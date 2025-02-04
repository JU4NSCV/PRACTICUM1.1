package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTBELONGTOCOLLECTION extends App {
  val pathSQLBelongToCollection = "src/main/INSERT_BELONG_TO_COLLECTION.sql"

  def InsertIntoBelongToCollection(data: List[(Int,Int, String, String, String)]): Boolean =
    def generateINSERT(row: (Int,Int, String, String, String)): String =
      s"INSERT INTO belongs_to_collection (id_movie, id, name, poster_path, backdrop_path) VALUE (${row._1},${row._2},'${row._3}','${row._4}','${row._5}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLBelongToCollection))
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
