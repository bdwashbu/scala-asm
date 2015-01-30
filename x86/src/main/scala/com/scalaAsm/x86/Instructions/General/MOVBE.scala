package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Move Data After Swapping Bytes
// Category: general/datamov

object MOVBE extends InstructionDefinition[OneOpcode]("MOVBE") with MOVBEImpl

trait MOVBEImpl {
  implicit object MOVBE_0 extends MOVBE._2[r16, m16] {
    def opcode = 0x38 /r
  }

  implicit object MOVBE_1 extends MOVBE._2[r32, m32] {
    def opcode = 0x38 /r
  }

  implicit object MOVBE_2 extends MOVBE._2[r64, m64] {
    def opcode = 0x38 /r
    override def prefix = REX.W(true)
  }

  implicit object MOVBE_3 extends MOVBE._2[m16, r16] {
    def opcode = 0x38 /r
  }

  implicit object MOVBE_4 extends MOVBE._2[m32, r32] {
    def opcode = 0x38 /r
  }

  implicit object MOVBE_5 extends MOVBE._2[m64, r64] {
    def opcode = 0x38 /r
    override def prefix = REX.W(true)
  }
}
