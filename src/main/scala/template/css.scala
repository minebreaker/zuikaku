package rip.deadcode.zuikaku
package template

import parse.Setting.Style

case class StyleProps(
    backgroundColor: String,
    fontFamily: String,
    fontSize: String,
    cellSize: String,
    cellBackgroundColor: String,
    cellBackgroundColorSecondary: String,
    animationDuration: String,
    raw: Option[String] = None
)

private val default = StyleProps(
  backgroundColor = "lightblue",
  fontFamily =
    """"Meiryo UI", "Noto Sans", "Helvetica Neue", Helvetica, system-ui, -apple-system, "Segoe UI", san-serif""",
  fontSize = "20px",
  cellSize = "200px",
  cellBackgroundColor = "cyan",
  cellBackgroundColorSecondary = "lightcyan",
  animationDuration = "0.1s",
  raw = None
)

extension (self: Option[Style])
  def toProps: StyleProps =
    self match
      case Some(config) =>
        StyleProps(
          backgroundColor = config.backgroundColor.getOrElse(default.backgroundColor),
          fontFamily = config.fontFamily.getOrElse(default.fontFamily),
          fontSize = config.fontSize.getOrElse(default.fontSize),
          cellSize = config.cellSize.getOrElse(default.cellSize),
          cellBackgroundColor = config.cellBackgroundColor.getOrElse(default.cellBackgroundColor),
          cellBackgroundColorSecondary = config.cellBackgroundColorSecondary
            .orElse(config.cellBackgroundColor)
            .getOrElse(default.cellBackgroundColorSecondary),
          animationDuration = config.animationDuration.getOrElse(default.animationDuration),
          raw = config.raw
        )
      case None => default

def renderCss(maybeProps: Option[Style]): String =
  val props = maybeProps.toProps
  import props.*

  s"""

${raw.getOrElse("")}

body {
    background-color: $backgroundColor;
    font-family: $fontFamily;
}

p {
  font-size: $fontSize;
}

.container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax($cellSize, 1fr));

    /* https://developer.mozilla.org/ja/docs/Web/CSS/grid-auto-flow */
    grid-auto-flow: dense;
    grid-auto-rows: $cellSize;
}

.container > *.cell:nth-child(2n) {
    background-color: $cellBackgroundColorSecondary;
}

.cell {
    background-color: $cellBackgroundColor;
}

.cell-image:hover {
    animation: cell-image $animationDuration ease-in;
}

@keyframes cell-image {
    50% {
        transform: rotate(4deg);
    }
}

.cell-image-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.grid-column-2 {
    grid-column: span 2;
}
.grid-column-3 {
    grid-column: span 3;
}
.grid-row-2 {
    grid-row: span 2;
}
.grid-row-3 {
    grid-row: span 3;
}


.modal {
    background-color: black;

    display: none;
    position: absolute;
    top: 0;
    width: 100%;
    height: 100%;

    animation: modal $animationDuration ease-in;
}

.modal.modal-closed {
    animation: modal-close $animationDuration ease-in;
}

@keyframes modal {
    from {
        transform: scale(0);
    }
}

@keyframes modal-close {
    to {
        transform: scale(0);
    }
}

.modal-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

a.cell-link {
    height: 100%;
    width: 100%;
    display: inline-block;

    color: inherit;
    text-decoration: none;
}
}

a.cell-link:visited {
    color: inherit;
}
""".stripMargin
