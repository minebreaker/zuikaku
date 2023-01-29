package rip.deadcode.zuikaku
package parse

import cats.effect.{IO, Resource}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveCodec

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

def parse[T](input: Path)(implicit decoder: Decoder[T]): IO[T] =
  import cats.syntax.either.*
  import io.circe.syntax.*
  import io.circe.yaml.v12.parser

  for
    jsonResult <- use(input) { r => parser.parse(r) }
    json <- jsonResult.liftTo[IO]
    definition <- json.as[T].liftTo[IO]
  yield definition
