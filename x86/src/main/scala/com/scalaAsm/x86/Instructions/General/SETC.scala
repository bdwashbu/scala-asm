package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Set Byte on Condition - below/not above or equal/carry (CF=1)
// Category: general/datamov

object SETC extends InstructionDefinition[OneOpcode]("SETC") with SETCImpl

trait SETCImpl {
  implicit object SETC_0 extends SETC._1[rm8] {
    def opcode = 0x92 /+ 0
  }
}
