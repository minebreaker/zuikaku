package rip.deadcode.zuikaku
package parse

import cats.syntax.functor.*
import io.circe.Decoder
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}

type I18nText = Map[String, String]

case class Setting(
    siteTitle: I18nText,
    style: Option[Setting.Style]
)

object Setting:
  case class Style(
      backgroundColor: Option[String],
      fontFamily: Option[String],
      fontSize: Option[String],
      gridGap: Option[String],
      cellSize: Option[String],
      cellBackgroundColor: Option[String],
      cellBackgroundColorSecondary: Option[String],
      cellShadow: Option[String],
      animationDuration: Option[String],
      raw: Option[String]
  )

  object Style:
    implicit val decoder: Decoder[Style] = deriveDecoder

  implicit val decoder: Decoder[Setting] = deriveDecoder

sealed trait Page

object Page:
  case class CellPage(title: I18nText, cells: List[Cell]) extends Page
  object CellPage:
    implicit val decoder: Decoder[CellPage] = deriveDecoder

  case class TextPage(
      title: I18nText,
      text: TextPage.Text
  ) extends Page

  object TextPage:
    case class Text(
        src: I18nText
    )
    implicit val decoder: Decoder[TextPage] = deriveDecoder

  implicit val decoder: Decoder[Page] = List[Decoder[Page]](
    Decoder[TextPage].widen,
    Decoder[CellPage].widen
  ).reduceLeft(_ or _)

sealed trait Cell
object Cell:
  case class Text(
      tag: Option[String],
      row: Option[Int],
      column: Option[Int],
      text: I18nText,
      link: Option[String]
  ) extends Cell
  object Text:
    implicit val decoder: Decoder[Text] = deriveDecoder

  case class Image(
      src: String,
      row: Option[Int],
      column: Option[Int],
      link: Option[String]
  ) extends Cell
  object Image:
    implicit val decoder: Decoder[Image] = deriveDecoder

  case class Raw(
      content: I18nText
  ) extends Cell
  object Raw:
    implicit val decoder: Decoder[Raw] = deriveDecoder

  implicit val decoder: Decoder[Cell] = List[Decoder[Cell]](
    Decoder[Text].widen,
    Decoder[Image].widen,
    Decoder[Raw].widen
  ).reduceLeft(_ or _)
