package com.scalaAsm.asm

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import com.scalaAsm.x86.InstructionResult
import com.scalaAsm.x86.Instructions.General._

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86._

import scala.reflect.runtime.universe._
import scala.util.control.Exception.allCatch
import java.lang.Long;
import java.lang.Byte;

object AsmMacro {

  val regList = Seq("ebx", "ebp", "eax", "ecx", "edx", "esp", "edi", "cl", "rsp", "rax", "spl")

  def isByte(s: String): Boolean = {
    if (s.contains("0x")) {
      Long.parseLong(s.drop(2), 16) < 256
    } else {
      (allCatch opt s.toByte).isDefined
    }
  }
  def isDword(s: String): Boolean = {
    if (s.contains("0x")) {
      (allCatch opt Long.parseLong(s.drop(2), 16)).isDefined
    } else {
      (allCatch opt s.toLong).isDefined
    }
  }

  def impl(c: Context)(args: c.Expr[Any]*): c.Expr[InstructionResult] = {
    import c.universe._
    import scala.reflect.runtime.{ currentMirror => cm }
    import scala.reflect.runtime.{ universe => ru }

    val asmInstruction = (c.prefix.tree match {
      case Apply(_, List(Apply(_, xs))) => xs map {
        case Literal(Constant(x: String)) => x
      }
      case _ => Nil
    }).head

    val params = Seq[Tree]((args map (_.tree)): _*)

    if (!params.isEmpty) {
      impl2(c)(args: _*)
    } else if (asmInstruction.endsWith(":")) { // label
      val labelName = Constant(asmInstruction.reverse.tail.reverse)
      c.Expr(q"Label($labelName)")
    } else if (asmInstruction.contains(' ') && !asmInstruction.contains(',')) {
      val mnemonic = asmInstruction.split(' ').head.toUpperCase()
      val param = asmInstruction.split(' ').last
      if (regList contains param) {
        impl2(c)(args: _*)
      } else if (!isDword(param)) {
        val varName = Constant(param)

        mnemonic match {
          case "CALL" => c.Expr(q"FunctionReference($varName)")
          case "PUSH" => c.Expr(q"Reference($varName)")
          case "JNZ" => c.Expr(q"""
                val ev = implicitly[JNZ#_1[Constant[_8]]]
                val format = implicitly[OneOperandFormat[Constant[_8]]]
                LabelRef($varName, ev, format)
                """)
          case "JZ" =>
            c.Expr(q"""
                val ev = implicitly[JZ#_1[Constant[_8]]]
                val format = implicitly[OneOperandFormat[Constant[_8]]]
                LabelRef($varName, ev, format)
                """)
          case "JE" =>
            c.Expr(q"""
                val ev = implicitly[JE#_1[Constant[_8]]]
                val format = implicitly[OneOperandFormat[Constant[_8]]]
                LabelRef($varName, ev, format)
                """)
          case "JMP" =>
            c.Expr(q"""
                val ev = implicitly[JMP#_1[Constant[_8]]]
                val format = implicitly[OneOperandFormat[Constant[_8]]]
                LabelRef($varName, ev, format)
                """)
          case "INVOKE" => c.Expr(q"Invoke($varName)")
          case _        => impl2(c)(args: _*)
        }
      } else {
        impl2(c)(args: _*)
      }
    } else {
      impl2(c)(args: _*)
    }
  }

  def parseInterpolated(c: Context, asmInstructions: List[String])(args: Seq[c.Expr[Any]]): c.Expr[InstructionResult] = {
    import c.universe._
    import scala.reflect.runtime.{ currentMirror => cm }
    import scala.reflect.runtime.{ universe => ru }

    val parts = c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        rawParts zip (args map (_.tree)) map {
          case (Literal(Constant(rawPart: String)), arg) =>
            arg.toString
        }
    }

    val fullInst = asmInstructions.reduce(_ + "" + _)
    val asmInstruction = asmInstructions.head
    val params = Seq[Tree]((args map (_.tree)): _*)

    val mnemonic = TermName(asmInstruction.split(' ').head.toUpperCase)
    //throw new Exception(params.head.toString())
    if (params.head.toString contains "toString") {
      val param = TermName(asmInstruction.split(' ').tail.mkString.split(',').head)
      // throw new Exception("kookoo")
      c.Expr(q"$mnemonic($param, dword(input))")
    } else if (!fullInst.contains(',')) {
      //throw new Exception("fuckcck")
      val x = q"${args(0)}"
      if (isByte(x.toString)) {
        c.Expr(q"$mnemonic(byte($x.toByte))")
      } else {
        c.Expr(q"$mnemonic($x)")
      }
    } else if (asmInstructions.size == 2 && asmInstructions.last.size == 0) { // interpolated var at end of string
      val param = TermName(asmInstruction.split(' ').tail.mkString.split(',').head)
      val paramString = parts(0).split('.').last
      val param2 = TermName(paramString)
      //throw new Exception(params(0).tpe.typeSymbol.name)
      //val x = q"${args(0)}"
      //throw new Exception(params(0).tpe.typeSymbol.name.toString)
      //c.Expr(q"$mnemonic($param, dword($param2.toInt))")
      params(0).tpe.typeSymbol.name.toString match {
        case "Byte"         => c.Expr(q"$mnemonic($param, byte($param2))")
        case "Long" | "Int" => c.Expr(q"$mnemonic($param, dword($param2))")
        case _              => c.Expr(q"$mnemonic($param, $param2)")
      }
    } else {
      val param = TermName(asmInstructions.last.split(' ').tail.mkString.split(',').head)
      val x = q"${args(0)}"
      c.Expr(q"$mnemonic($x, $param)")
    }
  }

  object TwoOperands {
    def unapply(line: String): Option[(String, String)] = {
      if (line.contains(' ') && line.contains(',')) {
        val params = line.split(' ').tail.reduce(_ + " " + _).split(',').map { param =>
          if (param.contains("(")) {
            param.trim.split("(").last.split(")").head
          } else {
            param.trim
          }
        }
        Some(params(0), params(1))
      } else {
        None
      }
    }
  }
  
  object OneOperand {
    def unapply(line: String): Option[String] = {
      if (line.contains(' ') && !line.contains(',')) {
        Some(line.split(' ').last)
      } else {
        None
      }
    }
  }
  
  object NoOperand {
    def unapply(line: String): Boolean = !line.contains(' ')
  }

  def impl2(c: Context)(args: c.Expr[Any]*): c.Expr[InstructionResult] = {
    import c.universe._
    import scala.reflect.runtime.{ currentMirror => cm }
    import scala.reflect.runtime.{ universe => ru }

    val parts = c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        rawParts zip (args map (_.tree)) map {
          case (Literal(Constant(rawPart: String)), arg) =>
            arg.toString
        }
    }

    val toolBox = currentMirror.mkToolBox()
    val importer = c.universe.mkImporter(ru)

    val asmInstructions = (c.prefix.tree match {
      case Apply(_, List(Apply(_, xs))) => xs map {
        case Literal(Constant(x: String)) => x
      }
      case _ => Nil
    })

    val asmInstruction = asmInstructions.head

    //         if (args.size > 0) {
    //           throw new Exception(c.internal.enclosingOwner.asClass.fullName)
    //         }

    // throw new Exception(c.internal.enclosingOwner.asClass.fullName)

    //throw new Exception(asmInstructions.reduce(_ + ", " + _))
    if (!args.isEmpty) { // contains an interpolated value
      parseInterpolated(c, asmInstructions)(args)
    } else {
      asmInstruction match {
      case NoOperand() =>
        val mnemonic = TermName(asmInstruction.toUpperCase())
        c.Expr(q"$mnemonic(())")
      case OneOperand(param) =>
        val mnemonic = TermName(asmInstruction.split(' ').head.toUpperCase())
        if (regList contains param) {
          val term1 = TermName(param)
          c.Expr(q"$mnemonic($term1)")
        } else {
          val term1 = Constant(param)
          if (isDword(param)) {
            c.Expr(q"$mnemonic(dword($term1.toInt))")
          } else {
            c.Expr(q"$mnemonic($term1)")
          }
        }
      case TwoOperands(operand1, operand2) =>
        val mnemonic = TermName(asmInstruction.split(' ').head.toUpperCase())

        if ((regList contains operand1) && (regList contains operand2)) {
          val term1 = TermName(operand1)
          val term2 = TermName(operand2)
          c.Expr(q"$mnemonic($term1, $term2)")
        } else if ((regList contains operand1) && operand2.split(" ").head == "byte") {
          //throw new Exception("FFFFFFFFFFFFF")
          val term1 = TermName(operand1)
          val constant = Constant(operand2.split(" ").last)
          c.Expr(q"$mnemonic($term1, byte($constant.toByte))")
        } else if ((regList contains operand1) && operand2.split(" ").head == "dword") {
          //throw new Exception("FFFFFFFFFFFFF")
          val term1 = TermName(operand1)
          val constant = Constant(operand2.split(" ").last)
          c.Expr(q"$mnemonic($term1, dword($constant.toInt))")
        } else if ((regList contains operand1) && isByte(operand2)) {

          val term1 = TermName(operand1)
          if (operand2.contains("0x")) {
            val constant = Constant(Integer.parseInt(operand2.drop(2), 16))
            c.Expr(q"$mnemonic($term1, byte($constant.toByte))")
          } else {
            val constant = Constant(operand2)
            c.Expr(q"$mnemonic($term1, byte($constant.toByte))")
          }
        } else if ((regList contains operand1) && isDword(operand2)) {
          //throw new Exception(params.reduce(_ + ", " + _))

          val term1 = TermName(operand1)
          if (operand2.contains("0x")) {
            val constant = Constant(Long.parseLong(operand2.drop(2), 16))
            c.Expr(q"$mnemonic($term1, dword($constant.toInt))")
          } else {
            val constant = Constant(operand2)
            c.Expr(q"$mnemonic($term1, dword($constant.toInt))")
          }
        } else {
          c.Expr(Apply(Select(This(TypeName("$anon")), mnemonic), List(Literal(Constant(())))))
        }
      }

      //Expr(Apply(Select(Select(Ident(TermName("$anon")), TermName("mov")), TermName("apply")), List(Select(Ident(TermName("$anon")), TermName("ebp")), Apply(Select(Ident(TermName("$anon")), TermName("esp")))))))

    }
  }

}