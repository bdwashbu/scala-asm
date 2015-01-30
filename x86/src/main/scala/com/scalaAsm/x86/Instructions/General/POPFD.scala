package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Pop Stack into eFLAGS Register
// Category: general/stackflgctrl

object POPFD extends InstructionDefinition[OneOpcode]("POPFD") with POPFDImpl

trait POPFDImpl {
  implicit object POPFD_0 extends POPFD._0 {
    def opcode = 0x9D
    override def hasImplicitOperand = true
  }
}
