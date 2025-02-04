import com.github.tototoshi.csv.*
import java.io.File
import play.api.libs.json.*
import play.api.libs.functional.syntax.*
//import movie_case_class.movie_cc
import scala.util.{Failure, Success, Try}

object Limpieza extends App {

  val rutaCsv = "data/pi_movies_complete.csv"
  implicit val csvFormat: DefaultCSVFormat = new DefaultCSVFormat {
    override val delimiter = ';' // Delimitador usado en el archivo
  }

  val reader = CSVReader.open(new File(rutaCsv))

  try {
    val datos = reader.allWithHeaders()

    // Filtrar filas vacías o con todas las columnas (excepto `id`) como `<unset>`
    val datosFiltrados = datos.filter { fila =>
      val columnasExcluyendoId = fila - "id"
      columnasExcluyendoId.values.exists(_.trim.nonEmpty) &&
        !columnasExcluyendoId.values.forall(valor => valor.trim.toLowerCase == "<unset>")
    }

    println(s"Total de filas después de eliminar vacías y <unset>: ${datosFiltrados.size}")


    println(s"Total de filas leídas: ${datos.size}")


    println(s"Total de filas después de eliminar vacías y <unset>: ${datosFiltrados.size}")

    // Validar cada fila y categorizar entre válidas e inválidas
    val resultados = datosFiltrados.map { fila =>
      Try {
        // Validar `id`: Debe ser un número
        val id = fila.getOrElse("id", "").trim
        require(id.matches("^\\d+$"), s"ID inválido: $id")

        // Validar `original_title`: No debe contener JSON ni ser nulo
        val originalTitle = fila.getOrElse("original_title", "").trim
        require(!originalTitle.startsWith("{") && !originalTitle.startsWith("["), s"JSON en original_title: $originalTitle")
        require(originalTitle.nonEmpty && !originalTitle.equalsIgnoreCase("null"), "Título original inválido")

        fila // Si todas las validaciones pasan, retorna la fila
      } match {
        case Success(validFila) => Right(validFila)
        case Failure(exception: Exception) => Left((fila, exception.getMessage))
        case scala.util.Failure(_) => ???
      }
    }

    // Separar filas válidas e inválidas
    val datosValidos = resultados.collect { case Right(fila: Map[String, String]) => fila }
    val datosInvalidos = resultados.collect { case Left((fila, error)) => (fila, error) }

    println(s"Total de filas válidas: ${datosValidos.size}")
    println(s"Total de filas inválidas: ${datosInvalidos.size}")

    if (datosInvalidos.nonEmpty) {
      println("\n=== Errores encontrados ===")
      datosInvalidos.take(10).foreach { case (_, error) =>
        println(s"- $error")
      }
    }


    // Limpieza de columnas
    val datosLimpios = datos.map { fila =>
      val adultLimpio = fila.getOrElse("adult", "").trim.toLowerCase match {
        case "true" | "1" | "yes" => "1"
        case "false" | "0" | "no" | "" => "0"
        case _ => "0" // Cubre cualquier caso no esperado
      }

      val budgetLimpio = try {
        val budgetStr = fila.getOrElse("budget", "0").replaceAll("[^\\d]", "").trim
        if (budgetStr.isEmpty) 0 else budgetStr.toInt.max(0)
      } catch {
        case _: Exception => 0 // Si ocurre un error, se asigna 0
      }

      val homepageLimpio = fila.getOrElse("homepage", "").trim match {
        case url if url.matches("^(https?|ftp)://[\\w.-]+(?:\\.[\\w.-]+)+[/\\w\\._~:?#[\\\\]@!$&'()*+,;=-]*$") => url
        case _ => "null" // Si no es un URL válido, se asigna "null"
      }

      val idLimpio = try {
        val idStr = fila.getOrElse("id", "0").replaceAll("[^\\d-]", "").trim
        if (idStr.startsWith("-")) idStr.substring(1) else idStr
      } catch {
        case _: Exception => "0" // Si ocurre un error, se asigna "0"
      }

      val imdbIdLimpio = fila.getOrElse("imdb_id", "").trim match {
        case imdb if imdb.matches("^tt\\d{7,8}$") => imdb
        case _ => "null" // Si no es un IMDB ID válido, se asigna "null"
      }

      val originalLanguageLimpio = fila.getOrElse("original_language", "").trim.toLowerCase match {
        case lang if lang.matches("^[a-z]{2,3}$") => lang // Valida que sea un código de idioma de 2 o 3 letras
        case _ => "null" // Si no cumple el formato, asigna "null"
      }

      val originalTitleLimpio = fila.getOrElse("original_title", "").trim match {
        // Limpieza básica
        case title if title.nonEmpty =>
          // 1. Limpiar el título
          val tituloLimpio = title.toLowerCase
            .replaceAll("[^\\w\\s:]", "") // Mantener letras, números, espacios y dos puntos
            .replaceAll("\\s{2,}", " ") // Reemplazar múltiples espacios consecutivos por uno solo
            .trim // Asegurar que no queden espacios al inicio o final

          // 2. Capitalizar: Primera letra en mayúscula, el resto en minúsculas.
          val tituloCapitalizado = tituloLimpio.split(" ").map(_.capitalize).mkString(" ")

          // Validar que el resultado no esté vacío
          if (tituloCapitalizado.isEmpty) "null" else tituloCapitalizado

        // Si el valor está vacío o es nulo.
        case _ => "null"
      }

      val overviewLimpio = fila.getOrElse("overview", "").trim match {
        // Limpieza básica
        case desc if desc.nonEmpty =>
          // 1. Convertir a minúsculas para normalizar el texto.
          val descripcionLimpia = desc.toLowerCase
            .replaceAll("[^\\w\\s.,:;!?']", "") // Mantener letras, números, espacios, y símbolos de puntuación básicos
            .replaceAll("\\s{2,}", " ") // Reemplazar múltiples espacios consecutivos por uno solo
            .trim // Asegurar que no queden espacios al inicio o final

          // 2. Capitalizar la primera letra del texto
          val descripcionCapitalizada = descripcionLimpia.capitalize

          // Validar que el resultado no esté vacío
          if (descripcionCapitalizada.isEmpty) "null" else descripcionCapitalizada

        // Si el valor está vacío o es nulo.
        case _ => "null"
      }

      val popularityLimpio = fila.getOrElse("popularity", "").trim match {
        case popularity if popularity.matches("^\\d+(\\.\\d+)?([eE][-+]?\\d+)?$") =>
          try {
            // Convertir el valor a formato decimal estándar
            BigDecimal(popularity.trim).setScale(6, BigDecimal.RoundingMode.HALF_UP).toString
          } catch {
            case _: Exception => "0.000000" // En caso de error, asignar un valor por defecto
          }
        case _ => "0.000000" // Si no es un número válido, asignar "0.000000"
      }

      val posterPathLimpio = fila.getOrElse("poster_path", "").trim match {
        // Validar si el valor cumple con un formato de archivo válido (ejemplo: "/nombre.jpg")
        case path if path.matches("^/[a-zA-Z0-9._-]+\\.(jpg|png|jpeg|gif)$") => path
        case _ => "null" // Si no cumple el formato, asignar "null"
      }

      val releaseDateLimpio = fila.getOrElse("release_date", "").trim match {
        case date if date.matches("^\\d{4}-\\d{2}-\\d{2}$") =>
          val Array(year, month, day) = date.split("-").map(_.toInt)

          // Ajustar el mes al rango válido (1 a 12)
          val mesAjustado = math.max(1, math.min(12, month))

          // Determinar el número máximo de días para el mes ajustado
          val diasEnMes = mesAjustado match {
            case 2 => if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28 // Febrero bisiesto
            case 4 | 6 | 9 | 11 => 30 // Abril, Junio, Septiembre, Noviembre
            case _ => 31 // Todos los demás meses
          }

          // Ajustar el día al rango válido para el mes ajustado
          val diaAjustado = math.max(1, math.min(diasEnMes, day))

          // Devolver la fecha ajustada en formato "yyyy-MM-dd"
          f"$year%04d-$mesAjustado%02d-$diaAjustado%02d"

        case _ => "null" // Si no cumple el formato básico, asignar "null"
      }

      val revenueLimpio = fila.getOrElse("revenue", "").trim match {
        case revenue if revenue.matches("^-?\\d+$") => // Permitir números positivos o negativos
          try {
            // Eliminar el signo negativo si está presente y convertir a número positivo
            math.abs(revenue.toLong).toString
          } catch {
            case _: Exception => "0" // Si ocurre un error (por ejemplo, un número muy grande), asignar 0
          }
        case _ => "0" // Si el valor no es un número válido, asignar 0
      }

      val runtimeLimpio = fila.getOrElse("runtime", "").trim match {
        case runtime if runtime.matches("^-?\\d+$") => // Permitir números enteros, positivos o negativos
          try {
            // Convertir a número entero y asegurarse de que no sea negativo
            math.abs(runtime.toInt).toString
          } catch {
            case _: Exception => "0" // Si ocurre un error (por ejemplo, un número muy grande), asignar 0
          }
        case _ => "0" // Si no es un número válido, asignar 0
      }

      val statusLimpio = fila.getOrElse("status", "").trim.toLowerCase match {
        // Valores válidos conocidos
        case status if Seq("released", "rumored", "post production", "planned", "in production", "canceled").contains(status) =>
          status.capitalize // Convertir la primera letra a mayúscula para consistencia

        // Manejo de valores mal formados (ejemplo: JSON o cadenas irreconocibles)
        case status if status.startsWith("{") || status.startsWith("[") => "null"

        // Valores vacíos o no válidos
        case _ => "null"
      }

      val taglineLimpio = fila.getOrElse("tagline", "").trim match {
        // Validar si el tagline es válido (no vacío, no símbolos irrelevantes como "...")
        case tagline if tagline.nonEmpty && !tagline.matches("^\\.+$") =>
          // Limpieza básica: eliminar múltiples espacios, dejar solo caracteres válidos
          val taglineLimpio = tagline
            .replaceAll("[^\\w\\s.,!?'-]", "") // Eliminar caracteres no válidos, dejando puntuación básica
            .replaceAll("\\s{2,}", " ") // Reducir múltiples espacios a uno
            .trim // Eliminar espacios al inicio y final

          // Validar si después de limpiar es válido
          if (taglineLimpio.nonEmpty) taglineLimpio.capitalize else "null"

        // Si el tagline está vacío o es irrelevante
        case _ => "null"
      }

      val titleLimpio = fila.getOrElse("title", "").trim match {
        // Validar si el título no está vacío
        case title if title.nonEmpty =>
          // 1. Limpiar el título
          val tituloLimpio = title.toLowerCase
            .replaceAll("[^\\w\\s:]", "") // Eliminar símbolos extraños, mantener letras, números, espacios y dos puntos
            .replaceAll("\\s{2,}", " ") // Reducir múltiples espacios consecutivos a uno solo
            .trim // Eliminar espacios al inicio o final

          // 2. Capitalizar la primera letra de cada palabra
          val tituloCapitalizado = tituloLimpio.split(" ").map(_.capitalize).mkString(" ")

          // Validar que el título no esté vacío después de limpiar
          if (tituloCapitalizado.isEmpty) "null" else tituloCapitalizado

        // Si el valor está vacío o es nulo
        case _ => "null"
      }

      val videoLimpio = fila.getOrElse("video", "").trim.toLowerCase match {
        // Convertir valores relacionados con "true" o equivalentes a 1
        case v if Seq("true", "1", "yes", "y", "enabled", "on").contains(v) => "1"
        // Convertir valores relacionados con "false" o equivalentes a 0
        case v if Seq("false", "0", "no", "n", "disabled", "off").contains(v) => "0"
        // Cualquier valor no reconocido se interpreta como falso (0)
        case _ => "0"
      }

      val voteAverageLimpio = fila.getOrElse("vote_average", "").trim match {
        case average if average.matches("^\\d+(\\.\\d+)?$") =>
          try {
            // Convertir a decimal y limitar a 1 decimal
            BigDecimal(average).setScale(1, BigDecimal.RoundingMode.HALF_UP).toString
          } catch {
            case _: Exception => "0.0" // En caso de error, asignar 0.0
          }
        case _ => "0.0" // Si no es un número válido, asignar 0.0
      }

      val voteCountLimpio = fila.getOrElse("vote_count", "").trim match {
        case count if count.matches("^-?\\d+$") =>
          try {
            // Convertir a número entero y eliminar el signo negativo si está presente
            math.abs(count.toInt).toString
          } catch {
            case _: Exception => "0" // Si ocurre un error (por ejemplo, un número muy grande), asignar 0
          }
        case _ => "0" // Si no es un número válido, asignar 0
      }


      Map(
        "adult" -> adultLimpio,
        "budget" -> budgetLimpio.toString,
        "homepage" -> homepageLimpio,
        "id" -> idLimpio, // FK
        "imdb_id" -> imdbIdLimpio,
        "original_language" -> originalLanguageLimpio,
        "original_title" -> originalTitleLimpio,
        "overview" -> overviewLimpio,
        "popularity" -> popularityLimpio,
        "poster_path" -> posterPathLimpio,
        "release_date" -> releaseDateLimpio,
        "revenue" -> revenueLimpio,
        "runtime" -> runtimeLimpio,
        "status" -> statusLimpio,
        "tagline" -> taglineLimpio,
        "title" -> titleLimpio,
        "video" -> videoLimpio,
        "vote_average" -> voteAverageLimpio,
        "vote_count" -> voteCountLimpio
      )
    }
    // Generar CSV limpio con los datos válidos y ya limpiados
    val rutaCsvLimpio = "data/pi_movie_small_cleaned_noJSON.csv"
    val writer = CSVWriter.open(new File(rutaCsvLimpio))

    val headers = Seq(
      "id", "adult", "budget", "homepage", "imdb_id", "original_language", "original_title",
      "overview", "popularity", "poster_path", "release_date", "revenue", "runtime",
      "status", "tagline", "title", "video", "vote_average", "vote_count"
    )
    writer.writeRow(headers)

    datosValidos.foreach { fila =>
      // Aquí se asegura que las columnas limpias se escriben
      val filaLimpia = Map(
        "id" -> fila.getOrElse("id", "0").trim,
        "adult" -> (if (fila.getOrElse("adult", "").trim.toLowerCase == "true") "1" else "0"),
        "budget" -> fila.getOrElse("budget", "0").replaceAll("\\D", ""),
        "homepage" -> fila.getOrElse("homepage", "null").trim,
        "imdb_id" -> fila.getOrElse("imdb_id", "null").trim,
        "original_language" -> fila.getOrElse("original_language", "null").trim.toLowerCase,
        "original_title" -> fila.getOrElse("original_title", "null").trim,
        "overview" -> fila.getOrElse("overview", "null").trim,
        "popularity" -> fila.getOrElse("popularity", "0.0").trim,
        "poster_path" -> fila.getOrElse("poster_path", "null").trim,
        "release_date" -> fila.getOrElse("release_date", "null").trim,
        "revenue" -> fila.getOrElse("revenue", "0").replaceAll("\\D", ""),
        "runtime" -> fila.getOrElse("runtime", "0").replaceAll("\\D", ""),
        "status" -> fila.getOrElse("status", "null").trim,
        "tagline" -> fila.getOrElse("tagline", "null").trim,
        "title" -> fila.getOrElse("title", "null").trim,
        "video" -> (if (fila.getOrElse("video", "").trim.toLowerCase == "true") "1" else "0"),
        "vote_average" -> fila.getOrElse("vote_average", "0.0").trim,
        "vote_count" -> fila.getOrElse("vote_count", "0").replaceAll("\\D", "")
      )

      // Escribir la fila limpia en el CSV
      val row = headers.map(header => filaLimpia.getOrElse(header, ""))
      writer.writeRow(row)
    }

    writer.close()
    println(s"CSV limpio generado en: $rutaCsvLimpio")

  } catch {
    case e: Exception =>
      println(s"Error: ${e.getMessage}")
  } finally {
    reader.close()
  }
}