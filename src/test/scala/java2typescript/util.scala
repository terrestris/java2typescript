package java2typescript

def wrapStatementJava(code: String) =
  s"class Wrap { void wrap() { $code } }"
