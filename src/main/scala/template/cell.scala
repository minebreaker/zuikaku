package rip.deadcode.zuikaku
package template

import parse.Cell

import cats.effect.IO

def renderCells(cells: List[Cell], lang: String): IO[String] =
  import cats.syntax.traverse.*
  for cells <- cells.traverse { cell =>
      renderCell(cell, lang)
    }
  yield s"""<div class="container">
           |  ${cells.mkString("\n")}
           |</div>
           |<div class="modal">
           |  <img class="modal-image">
           |</div>
           |""".stripMargin

def renderCell(cell: Cell, lang: String): IO[String] =
  cell match
    case c: Cell.Text  => renderTextCell(c, lang)
    case c: Cell.Image => IO.pure(renderImage(c, lang))
    case c: Cell.Raw   => IO.pure(renderRaw(c, lang))

private def renderGeneralCell(
    link: Option[String],
    row: Option[Int],
    column: Option[Int],
    inner: String,
    additionalClasses: Seq[String] = Seq.empty
): String =
  def renderOuter(inner: String) =
    val classes = (Seq(
      Some("cell"),
      row.map(n => s"grid-row-$n"),
      column.map(n => s"grid-column-$n")
    ).flatten ++ additionalClasses).mkString(" ")
    s"""<div class="$classes">
       |$inner
       |</div>
       |""".stripMargin

  link match
    case Some(link) =>
      renderOuter(
        s"""<a href="$link" class="cell-link">
           |  $inner
           |</a>""".stripMargin
      )
    case None => renderOuter(inner)

def renderTextCell(cell: Cell.Text, lang: String): IO[String] =
  import cats.syntax.option.*

  val tag = cell.tag.getOrElse("p")

  for
    originalText <- cell.text
      .get(lang)
      .liftTo[IO](RuntimeException(s"could not find the cell text for lang $lang. text=${cell.text("default")}"))
    text = originalText.replaceAll("\\n+", "<br/>")
  yield renderGeneralCell(
    cell.link,
    cell.row,
    cell.column,
    s"""<$tag>
       |  $text
       |</$tag>""".stripMargin,
    Seq("cell-text")
  )

def renderImage(cell: Cell.Image, lang: String): String =
  val useThumbnail = cell.column.getOrElse(1) == 1 && cell.row.getOrElse(1) == 1
  renderGeneralCell(
    cell.link,
    cell.row,
    cell.column,
    s"""<picture>
       |  <source srcset="${if (useThumbnail) getThumbnailFileName(cell.src) else cell.src}">
       |  <img src="${cell.src}" class="cell-image-image" decoding="async"></img>
       |</picture>""".stripMargin,
    additionalClasses = Seq(
      Some("cell-image"),
      cell.link.map(_ => "cell-image-link")
    ).flatten
  )

def renderRaw(cell: Cell.Raw, lang: String): String =
  renderGeneralCell(
    None,
    None,
    None,
    cell.content
      .get(lang)
      .orElse(cell.content.get("default"))
      .getOrElse(throw RuntimeException(s"could not find content for lang $lang nor default."))
  )
