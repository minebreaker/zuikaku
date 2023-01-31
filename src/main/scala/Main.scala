package rip.deadcode.zuikaku

import parse.Page.{CellPage, TextPage}
import parse.{Page, Setting}
import template.{renderCell, renderCells, renderCss, renderHtml, renderJs}

import cats.effect.{ExitCode, IO, IOApp, Resource}

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitOption, FileVisitResult, Files, Path, SimpleFileVisitor}
import scala.util.matching.Regex

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      config <- parseArgs(args.toArray)
      _ <- IO.println(config)
      _ <- process(config)
      _ <- IO.println("finished.")
    yield ExitCode.Success

private def process(config: Config): IO[Unit] =
  import cats.syntax.parallel.*

  import scala.jdk.StreamConverters.*

  for
    _ <- checkConditions(config)

    setting <- parse.parse[Setting](config.inRoot.resolve("setting.yaml"))

    paths <- use(Files.walk(config.inRoot)) { s =>
      s.toScala(List)
        .filter(f => Files.isRegularFile(f))
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
        case photoPat(_) =>
          processPhoto(config, path)
        case mdPat() | "setting.yaml" =>
          // just ignore
          IO.unit
        case unknown =>
          IO.raiseError(RuntimeException(s"Unknown file type: $unknown"))
    }
  yield ()

private def checkConditions(config: Config): IO[Unit] =
  import scala.jdk.StreamConverters.*

  for
    doesOutDirExist <- IO.blocking { Files.isDirectory(config.outRoot) }
    _ <- IO.whenA(doesOutDirExist) {
      if config.clean then
        for
          _ <- IO.println("cleaning output directory")
          _ <- IO.blocking {
            Files.walkFileTree(
              config.outRoot,
              new SimpleFileVisitor[Path] {
                override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
                  val r = super.visitFile(file, attrs)
                  Files.deleteIfExists(file)
                  r

                override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult =
                  val r = super.postVisitDirectory(dir, exc)
                  Files.deleteIfExists(dir)
                  r
              }
            )
          }
        yield ()
      else
        for
          isEmpty <- use(Files.list(config.outRoot)) { s => s.toScala(List).isEmpty }
          _ <- IO.raiseUnless(isEmpty)(RuntimeException("Output directory is not empty!"))
        yield ()
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
      node <- useReader(markdownFile) { r => markdownParser.parseReader(r) }
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
  val renderedCss = renderCss(setting.style)
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
  import cats.syntax.traverse.*

  val outDir = getOutDir(config, target.getParent)

  for _ <- Seq(
      removeGeotag(target, outDir),
      generateThumbnail(target, outDir, 200)
    ).parSequence
  yield ()
