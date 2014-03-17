package com.scalaAsm.x86
package Instructions

abstract class CALL extends x86Instruction("CALL")

trait CALL_1[-O1] extends CALL with OneOperand[O1] with OperandEncoding

object CALL {
  
  def call[O1](p1: O1)(implicit ev: CALL_1[O1]): CALL_1[O1] = ev
  
  implicit object call1 extends CALL_1[rm16] {
      val opcode = 0xFF /+ 2
      def operands = M(x)
  }
  
  implicit object call2 extends CALL_1[rm32] {
      val opcode = 0xFF /+ 3
      def operands = M(x)
  }
}