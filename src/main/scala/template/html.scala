package rip.deadcode.zuikaku
package template

import parse.Page.TextPage

def renderHtml(
    language: String,
    title: String,
    body: String
): String =

  val langTag = if (language == "default") "en" else language
  s"""
<!DOCTYPE html>
<html lang="$langTag}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>$title</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/normalize.css@8.0.1/normalize.css" integrity="sha384-M86HUGbBFILBBZ9ykMAbT3nVb0+2C7yZlF8X2CiKNpDOQjKroMJqIeGZ/Le8N2Qp" crossorigin="anonymous">
    <link rel="stylesheet" href="index.css">
</head>
<body>
$body
<script src="index.js"></script>
</body>
</html>
"""
