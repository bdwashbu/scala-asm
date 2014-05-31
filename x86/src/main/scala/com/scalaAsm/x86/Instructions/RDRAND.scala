package com.scalaAsm.x86
package Instructions

import com.scalaAsm.x86.Operands._
import scala.annotation.implicitNotFound

trait RDRAND extends x86Instruction {
  val mnemonic = "RDRAND"
}

trait RDRAND_1[OpEn, -O1 <: Operand] extends OneOperandInstruction[OpEn, O1] with RDRAND

object RDRAND extends Formats {
  
  implicit object rdrand1 extends RDRAND_1[M, rm32] {
      val opcode = (0x0F, 0xC7) /+ 6
  }
  
  implicit object rdrand2 extends RDRAND_1[M, rm16] {
      val opcode = (0x0F, 0xC7) /+ 6
  }
}