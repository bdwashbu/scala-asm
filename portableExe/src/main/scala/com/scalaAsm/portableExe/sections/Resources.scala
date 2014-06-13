package com.scalaAsm.portableExe
package sections

import java.nio.ByteBuffer
import com.scalaAsm.portableExe.ImageDataDirectory

object DirectoryTypeID extends Enumeration {
    type DirType = Value
    val cursor = Value(1)
    val bitmap = Value(2)
    val icon = Value(3)
    val menu = Value(4)
    val dialogBox = Value(5)
    val stringTableEntry = Value(6)
    val fontDirectory = Value(7)
    val font = Value(8)
    val acceleratorTable = Value(9)
    val applicationDefinedResource = Value(10)
    val messageTableEntry = Value(11)
    val groupCursor = Value(12)
    val groupIcon = Value(14)
    val versionInformation = Value(16)
    val dlginclude = Value(17)
    val plugAndPlay = Value(19)
    val VXD = Value(20)
    val animatedCursor = Value(21)
    val animatedIcon = Value(22)
    val html = Value(23)
    val assemblyManifest = Value(24)
}

object ImageResourceDirectory {

  def getResourceDir(input: ByteBuffer, beginningFileLocation: Int, level: Int, beginningOfSection: Int): ImageResourceDirectory = {
    val characteristics = input.getInt
    val timeDateStamp = input.getInt
    val majorVersion = input.getShort
    val minorVersion = input.getShort
    val numberOfNamedEntries = input.getShort
    val numberOfIdEntries = input.getShort

    val namedEntries = for (i <- 0 until numberOfNamedEntries) yield {
      ImageResourceDirectoryEntry.getNamedDirectoryEntry(input, beginningFileLocation, beginningOfSection, level)
    }

    val idEntries = for (i <- 0 until numberOfIdEntries) yield {
      ImageResourceDirectoryEntry.getIdDirectoryEntry(input, beginningFileLocation, beginningOfSection, level)
    }

    ImageResourceDirectory(
      characteristics,
      timeDateStamp,
      majorVersion,
      minorVersion,
      namedEntries,
      idEntries)
  }

  def getAllNamedEntries(root: ImageResourceDirectory): Seq[NamedImageResourceDirectoryEntry] = {
      val dirs = (root.namedEntries ++ root.idEntries).collect { case x: ImageResourceDirectoryEntry => x }
      dirs.collect { case x: NamedImageResourceDirectoryEntry => x } ++ dirs.flatMap { x => getAllNamedEntries(x.directory) }
  }
  
  def getResources(input: ByteBuffer, sections: Seq[SectionHeader], dir: ImageDataDirectory): ImageResourceDirectory = {

    val section = sections.find(section => section.virtualAddress <= dir.virtualAddress &&
      section.virtualAddress + section.sizeOfRawData > dir.virtualAddress)

    val resourceFileOffset = section.get.virtualAddress - section.get.pointerToRawData
    input.position(dir.virtualAddress - resourceFileOffset)
    val beginningFileLocation = input.position

    val resourceRoot = getResourceDir(input, beginningFileLocation, 0, resourceFileOffset)

    resourceRoot
  }
}

case class ImageResourceDirString(
  length: Short,
  name: String)

case class ImageResourceDirectory(
  characteristics: Int,
  timeDateStamp: Int,
  majorVersion: Short,
  minorVersion: Short,
  namedEntries: Seq[NamedImageResourceDirectoryEntry],
  idEntries: Seq[ImageResourceEntry])

trait ImageResourceEntry {
  val offsetToData: Int
}

trait ImageResourceDirectoryEntry extends ImageResourceEntry {
  val offsetToData: Int
  val directory: ImageResourceDirectory
}

case class NamedImageResourceDirectoryEntry(
  name: String,
  offsetToData: Int,
  directory: ImageResourceDirectory) extends ImageResourceDirectoryEntry

case class IdImageResourceDirectoryEntry(
  id: Int,
  offsetToData: Int,
  directory: ImageResourceDirectory) extends ImageResourceDirectoryEntry

object ImageResourceDirectoryEntry {
  def getNamedDirectoryEntry(input: ByteBuffer, beginningFileLocation: Int, beginningOfSection: Int, level: Int): NamedImageResourceDirectoryEntry = {
    val namePtr = input.getInt
    val offsetToData = input.getInt
    val savedPos = input.position
    //val result = if (namePtr >>> 31 == 1) {
    val pointer = namePtr & 0x7FFFFFFF
    input.position(beginningFileLocation + pointer)
    val stringLength = input.getShort
    var name: String = ""
    for (i <- 0 until stringLength) {
      name += input.getShort.toChar
    }
    var resDir: ImageResourceDirectory = null

    val savedPos2 = input.position
    input.position((offsetToData & 0x7FFFFFFF) + beginningFileLocation)
    if (level < 2) {
      resDir = ImageResourceDirectory.getResourceDir(input, beginningFileLocation, level + 1, beginningOfSection)
    }
    input.position(savedPos2)

    val result = NamedImageResourceDirectoryEntry(name, offsetToData, resDir)
    //}
    input.position(savedPos)
    result
  }

  def getIdDirectoryEntry(input: ByteBuffer, beginningFileLocation: Int, beginningOfSection: Int, level: Int): ImageResourceEntry = {
    val namePtr = input.getInt
    val offsetToData = input.getInt
    val savedPos = input.position
    val id = namePtr & 0x7FFFFFFF
    var result: ImageResourceEntry = null

    if (id == 0x409 || id == 0x415) {
      input.position((offsetToData & 0x7FFFFFFF) + beginningFileLocation)
      result = ImageResourceDataEntry.getDataEntry(input, beginningFileLocation)
    } else {
      val savedPos = input.position
      input.position((offsetToData & 0x7FFFFFFF) + beginningFileLocation)
      if (level < 2) {
        val resDir = ImageResourceDirectory.getResourceDir(input, beginningFileLocation, level + 1, beginningOfSection)
        result = IdImageResourceDirectoryEntry(id, offsetToData, resDir)
      }
      input.position(savedPos)
    }

    input.position(savedPos)
    result
  }
}

object ImageResourceDataEntry {
  def getDataEntry(input: ByteBuffer, beginningOfSection: Int): ImageResourceDataEntry = {
    val offset = input.getInt
    val size = input.getInt
    val codePage = input.getInt
    val reserved = input.getInt
    
    println("data " + (beginningOfSection))

    input.position(offset + beginningOfSection)
    
    val data = Array.fill(size)(0.toByte)
    input.get(data)
    ImageResourceDataEntry(
      offset,
      data,
      codePage,
      reserved)
  }
}

case class ImageResourceDataEntry(
  offsetToData: Int,
  data: Array[Byte],
  codePage: Int,
  reserved: Int) extends ImageResourceEntry