package com.tosware.nkm.models

import com.tosware.nkm.UseCheck
import com.tosware.nkm.models.CommandResponse.CommandResponse

object UseCheck {
  def canBeUsed(useChecks: Set[UseCheck]): CommandResponse = {
    val failures = useChecks.filter(_._1 == false)
    if (failures.isEmpty) CommandResponse.Success()
    else CommandResponse.Failure(failures.map(_._2).mkString("\n"))
  }
}
