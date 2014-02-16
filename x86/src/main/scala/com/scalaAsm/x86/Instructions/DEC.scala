package com.scalaAsm.x86.Instructions

import com.scalaAsm.x86._
import x86Registers._

trait DEC

trait DEC_1[-O1] extends DEC {
  def get(p1: O1): Instruction
}

object DEC {

  protected[this] abstract class O[X <: OperandSize](op1: ModRM.reg[X]) extends Instruction1[ModRM.reg[X]] {
   val opcodeExtension = None
   val operand1 = op1
  }
  
  implicit object dec1 extends DEC_1[r32] {
    def get(x: r32) = new O[DwordOperand](x) {
      val opcode = (0x48 + x.ID).toByte
      val modRM: Option[AddressingFormSpecifier] = Some(getAddressingForm1(this))
    }
  }
  
  implicit object dec2 extends DEC_1[r16] {
    def get(x: r16) = new O[WordOperand](x) {
	    val opcode = (0x48 + x.ID).toByte
	    val modRM: Option[AddressingFormSpecifier] = Some(getAddressingForm1(this))
     }
  }
}