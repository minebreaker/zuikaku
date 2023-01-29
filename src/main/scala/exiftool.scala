package rip.deadcode.zuikaku

import cats.effect.IO

import java.nio.file.Path

def removeGeotag(inFile: Path, outDir: Path): IO[Unit] =

  val out = outDir.resolve(inFile.getFileName)

  val args = Seq(
    "exiftool",
    inFile.toString,
    "-gps:all=",
    "-U", // extract unknown tags
    "-o",
    out.toString
  )

  for
    _ <- IO.println(args)
    result <- execute(
      args
    )
    (code, _, err) = result
    _ <- IO.raiseUnless(code == 0)(RuntimeException(s"ffmpeg has exited with status code $code\nstderr:\n$err"))
  yield ()
