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

private def retry[A](io: IO[A], times: Int): IO[A] =
  import scala.concurrent.duration.*
  io.handleErrorWith { e =>
    if times > 0 then
      IO.println(s"Exception occurred. Retrying(${times - 1})...") *>
        IO.sleep(1.seconds) *>
        retry(io, times - 1)
    else IO.raiseError(RuntimeException("Retry threshold exceeded", e))
  }

def execute(commands: Seq[String]): IO[(Int, String, String)] =
  val f = IO.blocking {
    val pb = Process(commands)

    val stdout = StringBuilder()
    val stderr = StringBuilder()
    val p = pb.run(ProcessLogger(stdout.append, stderr.append))
    val code = p.exitValue()

    (code, stdout.toString(), stderr.toString())
  }
  retry(f, 3)
