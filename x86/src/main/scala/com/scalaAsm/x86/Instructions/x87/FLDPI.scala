package com.scalaAsm.x86
package Instructions
package x87

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Load Constant π
// Category: general/ldconst

object FLDPI extends InstructionDefinition[OneOpcode]("FLDPI") with FLDPIImpl

trait FLDPIImpl {
  implicit object FLDPI_0 extends FLDPI._0 {
    def opcode = 0xD9 /+ 5
    override def hasImplicitOperand = true
  }
}
