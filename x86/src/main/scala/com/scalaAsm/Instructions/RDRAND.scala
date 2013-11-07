package com.scalaAsm.x86.Instructions

import com.scalaAsm.x86._
import x86Registers._
import Addressing._
import MODRM._
import scala.annotation.implicitNotFound
import com.scalaAsm.utils.Endian

trait RDRAND
trait RDRAND_M[-O1] extends RDRAND {
  def get(p1: O1): Array[Byte]
}

object RDRAND {
  implicit object rdrand1 extends RDRAND_M[r32] { def get(x: r32) = Array[Byte](0x0F.toByte, 0xC7.toByte) ++ modRM(x, reg = 6.toByte) }
  implicit object rdrand2 extends RDRAND_M[r16] { def get(x: r16) = Array[Byte](0x0F.toByte, 0xC7.toByte) ++ modRM(x, reg = 6.toByte) }
  //implicit object rdrand3 extends RDRAND_M[r64] { def get(x: r32) = Array[Byte](0x0F.toByte, 0xC7.toByte) ++ modRM(x, reg = 6.toByte) }
}