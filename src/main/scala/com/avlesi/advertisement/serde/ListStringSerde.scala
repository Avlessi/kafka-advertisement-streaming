package com.avlesi.advertisement.serde

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.lightbend.kafka.scala.streams.StatelessScalaSerde


class ListStringSerde extends StatelessScalaSerde[List[String]] {

  override def serialize(data: List[String]): Array[Byte] = {
    try {
      val byteOut = new ByteArrayOutputStream()
      val objOut = new ObjectOutputStream(byteOut)
      objOut.writeObject(data)
      objOut.close()
      byteOut.close()
      byteOut.toByteArray
    }
    catch {
      case ex:Exception => throw new Exception(ex.getMessage)
      //TODO
    }
  }

  override def deserialize(data: Array[Byte]): Option[List[String]] = {
    val byteIn = new ByteArrayInputStream(data)
    val objIn = new ObjectInputStream(byteIn)
    val obj = objIn.readObject().asInstanceOf[List[String]]
    byteIn.close()
    objIn.close()
    Option(obj)
  }
}