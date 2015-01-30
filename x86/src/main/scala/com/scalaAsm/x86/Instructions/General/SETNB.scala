package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Set Byte on Condition - not below/above or equal/not carry (CF=0)
// Category: general/datamov

object SETNB extends InstructionDefinition[OneOpcode]("SETNB") with SETNBImpl

trait SETNBImpl {
  implicit object SETNB_0 extends SETNB._1[rm8] {
    def opcode = 0x93 /+ 0
  }
}
