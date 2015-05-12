//import play.Project._

name := "scala_x86"

version := "0.1-SNAPSHOT"

organization := "com.scalaAsm"

scalaVersion := "2.11.6"

//playScalaSettings

lazy val scalax86 = project.in(file(".")).aggregate(x86, coff, asm, assembler, portableExe, linker, example)
    
lazy val example = project.in(file("example"))
    .dependsOn(x86, asm, assembler, linker)
    
lazy val coff = project.in(file("coff"))

lazy val x86 = project.in(file("x86"))

lazy val asm = project.in(file("asm"))
    .dependsOn(x86, coff, asmMacro)

lazy val asmMacro = project.in(file("asmMacro"))
    .dependsOn(x86)

lazy val portableExe = project.in(file("portableExe"))
    .dependsOn(x86, coff)

lazy val linker = project.in(file("linker"))
    .dependsOn(coff, portableExe)

lazy val assembler = project.in(file("assembler"))
    .dependsOn(x86, coff, asm)