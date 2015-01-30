package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Set Byte on Condition - not less nor equal/greater ((ZF=0) AND (SF=OF))
// Category: general/datamov

object SETNLE extends InstructionDefinition[OneOpcode]("SETNLE") with SETNLEImpl

trait SETNLEImpl {
  implicit object SETNLE_0 extends SETNLE._1[rm8] {
    def opcode = 0x9F /+ 0
  }
}
