package rip.deadcode.zuikaku

import cats.effect.IO

import java.nio.file.Path

def generateThumbnail(inFile: Path, outDir: Path, size: Int): IO[Unit] =

  val outFileName = getThumbnailFileName(inFile.getFileName.toString)
  val out = outDir.resolve(outFileName)

  val args = Seq(
    "ffmpeg",
    "-i",
    inFile.toString,
    "-vf",
    s"scale=$size:$size:force_original_aspect_ratio=increase",
    out.toString
  )

  for
    _ <- IO.println(args)
    result <- execute(
      args
    )
    (code, _, err) = result
    _ <- IO.println(s"ffmpeg $code")
    _ <- IO.raiseUnless(code == 0)(RuntimeException(s"ffmpeg has exited with status code $code\nstderr:\n$err"))
  yield ()

def getThumbnailFileName(fileName: String): String = fileName.replaceFirst("\\.[^.]{3,4}$", ".thumbnail.jpg")

//def minify(inFile: Path): IO[Unit] =
//
//  val args = Seq(
//    "ffmpeg",
//    "-y", // overwrite the out file
//    "-i",
//    inFile.toString,
//    // just use a default setting
//    inFile.toString
//  )
//
//  for
//    _ <- IO.println(args)
//    result <- execute(
//      args
//    )
//    (code, _, err) = result
//    _ <- IO.println(s"ffmpeg $code")
//    _ <- IO.raiseUnless(code == 0)(RuntimeException(s"ffmpeg has exited with status code $code\nstderr:\n$err"))
//  yield ()
