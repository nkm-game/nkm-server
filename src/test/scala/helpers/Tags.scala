package helpers

import org.scalatest.Tag

/** Not working on CI:
  *
  *   - observing game events
  *
  *   - retrieving relative files
  *
  *   - retrieving version file
  */
object NotWorkingOnCI extends Tag("not-working-on-ci")
