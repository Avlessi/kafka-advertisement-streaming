package com.avlesi.advertisement

import com.avlesi.advertisement.serde.ListStringSerde
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ListStringSerdeTest extends FunSuite {

  test("test list string serde") {

    val stringListSerde = new ListStringSerde
    val list = List[String]("a", "b", "c")
    val serList:Array[Byte] = stringListSerde.serialize(list)

    val deserList: Option[List[String]] = stringListSerde.deserialize(serList)

    assert(list === deserList.get)
  }
}
