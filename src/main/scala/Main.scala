package rip.deadcode.zuikaku

import parse.{Page, Setting}
import parse.Page.{CellPage, TextPage}
import template.{renderCell, renderCells, renderCss, renderHtml, renderJs}

import cats.effect.{ExitCode, IO, IOApp, Resource}

import java.nio.file.{FileVisitOption, Files, Path}
import scala.util.matching.Regex

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      config <- parseArgs(args.toArray)
      _ <- IO.println(config)
      _ <- process(config)
    yield ExitCode.Success

private def process(config: Config): IO[Unit] =
  import cats.syntax.parallel.*

  import scala.jdk.StreamConverters.*

  for
    doesOutDirExist <- IO.blocking { Files.isDirectory(config.outRoot) }
    _ <- IO.whenA(doesOutDirExist) {
      Resource.fromAutoCloseable(IO.blocking(Files.list(config.outRoot))).use { s =>
        IO.raiseUnless(s.toScala(List).isEmpty)(RuntimeException("Output directory is not empty!"))
      }
    }

    setting <- parse.parse[Setting](config.inRoot.resolve("setting.yaml"))

    paths <- Resource.fromAutoCloseable(IO.blocking(Files.walk(config.inRoot))).use { s =>
      IO.blocking {
        s.toScala(List)
          .filter(f => Files.isRegularFile(f))
      }
    }
    _ <- paths.parTraverse { path =>
      val fileName = path.getFileName.toString
      val mdPat = "^.*\\.md$".r
      val photoPat = "^.*\\.(jpg|jpeg|png|bmp|JPG|JPEG|PNG|BMP)$".r
      fileName match
        case "index.yaml" =>
          for
            definition <- parse.parse[Page](path)
            _ <- definition match
              case d: CellPage => processCell(config, setting, path.getParent, d)
              case d: TextPage => processTextPage(config, setting, path.getParent, d)
          yield ()
        case mdPat() | "setting.yaml" =>
          // just ignore
          IO.unit
        case photoPat(_) =>
          processPhoto(config, path)
        case unknown =>
          IO.raiseError(RuntimeException(s"Unknown file type: $unknown"))
    }
  yield ()

private def processCell(config: Config, setting: Setting, processingDir: Path, page: CellPage): IO[Unit] =
  import cats.syntax.option.*
  import cats.syntax.traverse.*

  for _ <- page.title.toList.traverse { case (lang, title) =>
      for

        renderedCells <- renderCells(page.cells, lang)

        siteTitle <- setting.siteTitle.get(lang).liftTo[IO](???)

        renderedHtml = renderHtml(
          lang,
          s"$title - $siteTitle",
          renderedCells
        )

        outDir = getOutDir(config, processingDir, lang)

        outHtmlPath = outDir.resolve("index.html")
        _ <- write(outHtmlPath, renderedHtml)

        _ <- IO.whenA(lang == "default") {
          processIndexAssets(setting, outDir)
        }
      yield ()
    }
  yield ()

private val markdownParser = com.vladsch.flexmark.parser.Parser.builder().build()
private val htmlRenderer = com.vladsch.flexmark.html.HtmlRenderer.builder().build()
private def processTextPage(config: Config, setting: Setting, processingDir: Path, page: TextPage): IO[Unit] =
  import cats.syntax.option.*
  import cats.syntax.traverse.*

  page.text.src.toList.traverse { case (lang, src) =>
    val markdownFile = processingDir.resolve(src)
    for
      node <- use(markdownFile) { r => markdownParser.parseReader(r) }
      renderedMarkdown = htmlRenderer.render(node)

      title <- page.title
        .get(lang)
        .liftTo[IO](RuntimeException(s"There is a file for lang $lang but could not found the corresponding title"))
      renderedHtml = renderHtml(
        lang,
        title,
        renderedMarkdown
      )

      outDir = getOutDir(config, processingDir, lang)
      outHtmlPath = outDir.resolve("index.html")
      _ <- write(outHtmlPath, renderedHtml)

      _ <- IO.whenA(lang == "default") {
        processIndexAssets(setting, outDir)
      }
    yield ()
  }.void

private def processIndexAssets(setting: Setting, outDir: Path): IO[Unit] =
  val renderedCss = renderCss(raw = setting.style.flatMap(_.raw))
  val outCssPath = outDir.resolve("index.css")
  for
    _ <- write(outCssPath, renderedCss)

    renderedJs = renderJs()
    outJsPath = outDir.resolve("index.js")
    _ <- write(outJsPath, renderedJs)
  yield ()

private def getOutDir(config: Config, processingDir: Path, lang: String = "default"): Path =
  if lang == "default" then
    val relative = config.inRoot.relativize(processingDir)
    config.outRoot.resolve(relative)
  else
    val relative = config.inRoot.relativize(processingDir)
    config.outRoot.resolve("i18n").resolve(lang).resolve(relative)

private def processPhoto(config: Config, target: Path): IO[Unit] =
  import cats.syntax.parallel.*

  val outDir = getOutDir(config, target.getParent)
  Seq(
    removeGeotag(target, outDir),
    generateThumbnail(target, outDir, 200)
  ).parSequence.void
