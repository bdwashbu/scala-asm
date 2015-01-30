package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Jump short if less/not greater (SF!=OF)
// Category: general/branch/cond

object JNGE extends InstructionDefinition[OneOpcode]("JNGE") with JNGEImpl

trait JNGEImpl {
  implicit object JNGE_0 extends JNGE._1[rel8] {
    def opcode = 0x7C
  }

  implicit object JNGE_1 extends JNGE._1[rel16] {
    def opcode = 0x8C
  }

  implicit object JNGE_2 extends JNGE._1[rel32] {
    def opcode = 0x8C
  }
}
