package com.tosware.nkm.models

case class NkmColor(name: String, r: Int, g: Int, b: Int)

object NkmColor {
  val availableColors: Seq[NkmColor] = Seq(
    NkmColor("Red", 255, 0, 0),
    NkmColor("Blue", 0, 0, 255),
    NkmColor("Green", 0, 128, 0),
    NkmColor("Yellow", 255, 255, 0),
    NkmColor("Purple", 128, 0, 128),
    NkmColor("Orange", 255, 165, 0),
    NkmColor("Black", 0, 0, 0),
    NkmColor("White", 255, 255, 255),
    NkmColor("Gray", 128, 128, 128),
    NkmColor("Brown", 165, 42, 42),
    NkmColor("Pink", 255, 192, 203),
    NkmColor("Teal", 0, 128, 128),
    NkmColor("Maroon", 128, 0, 0),
    NkmColor("Olive", 128, 128, 0),
    NkmColor("Navy Blue", 0, 0, 128),
    NkmColor("Cyan", 0, 255, 255),
    NkmColor("Magenta", 255, 0, 255),
    NkmColor("Lime Green", 50, 205, 50),
    NkmColor("Turquoise", 64, 224, 208),
    NkmColor("Coral", 255, 127, 80),
    NkmColor("Violet", 238, 130, 238),
    NkmColor("Gold", 255, 215, 0),
    NkmColor("Silver", 192, 192, 192),
    NkmColor("Beige", 245, 245, 220),
    NkmColor("Emerald Green", 0, 128, 0),
    NkmColor("Sapphire Blue", 15, 82, 186),
    NkmColor("Ruby Red", 155, 17, 30),
    NkmColor("Amber", 255, 191, 0),
    NkmColor("Mint Green", 152, 255, 152),
    NkmColor("Charcoal", 54, 69, 79),
  )

  val availableColorNames: Seq[String] = availableColors.map(_.name)
}
