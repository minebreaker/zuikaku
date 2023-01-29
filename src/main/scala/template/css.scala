package rip.deadcode.zuikaku
package template

def renderCss(
    backgroundColor: String ="lightblue",
    cellSize: String = "200px",
    cellBackgroundColor: String = "cyan",
    animationDuration: String = "0.1s",
    raw: Option[String] = None
): String =
  s"""

${ raw.getOrElse("") }

body {
    background-color: $backgroundColor;
}

.container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax($cellSize, 1fr));

    /* https://developer.mozilla.org/ja/docs/Web/CSS/grid-auto-flow */
    grid-auto-flow: dense;
    grid-auto-rows: $cellSize;
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
