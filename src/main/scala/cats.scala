package rip.deadcode.zuikaku

import cats.effect.{IO, Resource}

import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.nio.file.{FileVisitOption, Files, OpenOption, Path, StandardOpenOption}
import scala.concurrent.Future
import scala.sys.process.{BasicIO, Process, ProcessImplicits, ProcessLogger}

def use[T, R <: AutoCloseable](f: => R)(g: R => T): IO[T] =
  Resource.fromAutoCloseable(IO.blocking(f)).use { r =>
    IO.blocking {
      g(r)
    }
  }

def useReader[T](path: Path)(g: BufferedReader => T): IO[T] =
  use(Files.newBufferedReader(path, StandardCharsets.UTF_8)) { r =>
    g(r)
  }

def write(path: Path, s: String): IO[Unit] =
  IO.blocking {
    Files.createDirectories(path.getParent)
    Files.write(path, s.getBytes(StandardCharsets.UTF_8))
  }

def execute(commands: Seq[String]): IO[(Int, String, String)] =
  IO.blocking {
    val pb = Process(commands)

    val stdout = StringBuilder()
    val stderr = StringBuilder()
    val p = pb.run(ProcessLogger(stdout.append, stderr.append))
    val code = p.exitValue()

    (code, stdout.toString(), stderr.toString())
  }
