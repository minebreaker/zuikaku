package rip.deadcode.zuikaku

import cats.effect.IO
import com.google.common.base.Preconditions.checkArgument
import org.apache.commons.cli.{DefaultParser, Options}

import java.nio.file.{Files, Path, Paths}
import scala.util.Try

case class Config(
    inRoot: Path,
    outRoot: Path,
    clean: Boolean
)

private val options = Options()
  .addRequiredOption("i", "in", true, "Input root directory.")
  .addRequiredOption("o", "out", true, "Output root directory.")
  .addOption(null, "clean", false, "Clean the output directory.")

def parseArgs(args: Array[String]): IO[Config] =
  val parser = DefaultParser()

  for
    result <- IO {
      parser.parse(options, args)
    }

    inRootStr = result.getOptionValue("in")
    inRoot <- strToDir(inRootStr, requireExists = true)
    
    outRootStr = result.getOptionValue("out")
    outRoot <- strToDir(outRootStr)
    
    clean = result.hasOption("clean")
    
  yield Config(
    inRoot,
    outRoot,
    clean
  )

private def strToDir(s: String, requireExists: Boolean = false): IO[Path] =
  val path = Paths.get(s).toAbsolutePath.normalize()
  if (requireExists)
    for
      fileExists <- IO.blocking(Files.exists(path))
      isDirectory <- IO.blocking(Files.isDirectory(path))
      _ <- IO.raiseUnless(fileExists)(RuntimeException(s"Not a directory: $path"))
      _ <- IO.raiseUnless(isDirectory)(RuntimeException(s"Directory does not exists: $path"))
    yield path
  else
    IO.pure(path)
