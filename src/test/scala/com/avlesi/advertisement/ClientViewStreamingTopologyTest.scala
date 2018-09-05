//package com.avlesi.advertisement
//
//import java.util.{Collections, Properties, UUID}
//
//import com.avlesi.advertisement.model.{AdAnnouncementRuleClient, _}
//import com.avlesi.advertisement.serde.ListStringSerde
//import com.avlesi.advertisement.streaming.ClientViewStreaming
//import com.fasterxml.jackson.databind.{JsonNode, JsonSerializable, ObjectMapper}
//import org.apache.kafka.streams.TopologyTestDriver
//import org.junit.runner.RunWith
//import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
//import org.scalatest.junit.JUnitRunner
//import org.apache.kafka.streams.StreamsConfig
//import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
//import com.lightbend.kafka.scala.streams.DefaultSerdes._
//import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer, StringSerializer}
//import org.apache.kafka.connect.json.JsonDeserializer
//import org.apache.kafka.connect.json.JsonSerializer
//import org.apache.kafka.streams.test.OutputVerifier
//
//@RunWith(classOf[JUnitRunner])
//class ClientViewStreamingTopologyTest extends FunSuite with BeforeAndAfter {
//
//  var testDriver: TopologyTestDriver = _
//  val adClientTopic = "ad-client-input-topic"
//  val adRuleTopic = "ad-rule-input-topic"
//  val adAnnouncementTopic = "ad-announcement-input-topic"
//  val adClientAnnouncementOutputTopic = "ad-client-announcement-output-topic"
//  val serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081")
//
//  val valueSpecificAdClientAvroSerde = new SpecificAvroSerde[AdClient]
//  valueSpecificAdClientAvroSerde.configure(serdeConfig, false)
//
//  val valueSpecificAdRuleAvroSerde = new SpecificAvroSerde[AdRule]
//  valueSpecificAdRuleAvroSerde.configure(serdeConfig, false)
//
//  val valueSpecificAdAnnouncementAvroSerde = new SpecificAvroSerde[AdAnnouncement]
//  valueSpecificAdAnnouncementAvroSerde.configure(serdeConfig, false)
//
//  val valueSpecificAdAnnouncementRuleClientAvroSerde = new SpecificAvroSerde[AdAnnouncementRuleClient]
//  valueSpecificAdAnnouncementRuleClientAvroSerde.configure(serdeConfig, false)
//
//  before {
////    println("before")
////    val config = new Properties
////    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "advertisement-stream-test-" + UUID.randomUUID())
////    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy")
////    val topology = ClientViewStreaming.createTopology()
////    testDriver = new TopologyTestDriver(topology, config)
//
////    val config = new Properties
////    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "someTest-" + UUID.randomUUID())
////    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy")
////    val topology = ClientViewStreaming.wordCountTopology()
////    testDriver = new TopologyTestDriver(topology, config)
//
//
//    val config = new Properties
//    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "someTest-" + UUID.randomUUID())
//    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy")
//    config.put("schema.registry.url", "http://localhost:8081")
//    config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
//    val topology = ClientViewStreaming.tempTopology()
//    testDriver = new TopologyTestDriver(topology, config)
//  }
//
//
//  test("tmpTest") {
//    val clientFactory = new ConsumerRecordFactory[String, AdClient](adClientTopic,
//      new StringSerializer,
//      valueSpecificAdClientAvroSerde.serializer())
//
//    val ruleFactory = new ConsumerRecordFactory[String, AdRule](adRuleTopic,
//      new StringSerializer,
//      valueSpecificAdRuleAvroSerde.serializer())
//
//    val adAnnouncementFactory = new ConsumerRecordFactory[String, AdAnnouncement](
//      adAnnouncementTopic,
//      new StringSerializer,
//      valueSpecificAdAnnouncementAvroSerde.serializer())
//
//    val adClient = AdClient(Option("100"), Option("Sasha"))
//
//    testDriver.pipeInput(clientFactory.create(adClientTopic, adClient.id.get, adClient))
//
//    val adRule = AdRule(
//      Option("rule0"),
//      Option("france rule"),
//      Option("10"),
//      Option(Rule(
//        Option(Seq("male", "female")),
//        Option(Seq("France")))))
//
//    testDriver.pipeInput(ruleFactory.create(adRuleTopic, adRule.id.get, adRule))
//
//    val adAnnouncement = AdAnnouncement(
//      Option("10"),
//      Option("ad-10"),
//      Option("some text"),
//      Option("science")
//    )
//
//    testDriver.pipeInput(adAnnouncementFactory.create(adAnnouncementTopic,
//      adAnnouncement.id.get,
//      adAnnouncement))
//
//
//    val clientOutput = testDriver.readOutput("clientTopic1",
//      new StringDeserializer(), valueSpecificAdClientAvroSerde.deserializer())
//    println(clientOutput)
//    println(clientOutput.key())
//    println(clientOutput.value())
//
//    val ruleOutput = testDriver.readOutput("ruleTopic1",
//      new StringDeserializer(), valueSpecificAdRuleAvroSerde.deserializer())
//    println(ruleOutput)
//    println(ruleOutput.key())
//    println(ruleOutput.value())
//
//    val ruleListOutput = testDriver.readOutput("ruleListTopic",
//      new StringDeserializer(), new ListStringSerde().deserializer())
//    println(ruleListOutput)
//    println(ruleListOutput.key())
//    println(ruleListOutput.value())
//
//
//    val specialRuleKeyClientOutput = testDriver.readOutput("specialRuleKeyClientTopic",
//      new StringDeserializer(), valueSpecificAdClientAvroSerde.deserializer())
//    println(specialRuleKeyClientOutput)
//    println(specialRuleKeyClientOutput.key())
//    println(specialRuleKeyClientOutput.value())
//
//    val clientRulesOutput = testDriver.readOutput("clientRulesStreamTopic",
//      new StringDeserializer(), new StringDeserializer)
//    println(clientRulesOutput)
//    println(clientRulesOutput.key())
//    println(clientRulesOutput.value())
//
//
//    //    val cleanedClientRulesOutput = testDriver.readOutput("cleanedClientRulesTopic",
////      valueSpecificAdClientAvroSerde.deserializer(), new ListStringSerde().deserializer())
////    println(cleanedClientRulesOutput)
////    println(cleanedClientRulesOutput.key())
////    println(cleanedClientRulesOutput.value())
//
//
//  }
//
////  test("some") {
////
////    val recordFactory: ConsumerRecordFactory[String, String] =
////      new ConsumerRecordFactory(new StringSerializer, new StringSerializer)
////    val value = "testing Kafka Streams"
////    testDriver.pipeInput(recordFactory.create("word-count-input", null, value))
////    val output = testDriver.readOutput("word-count-output",
////      new StringDeserializer(), new LongDeserializer())
////    println(output)
////    println(output.key())
////    println(output.value())
////  }
//
//
//
//
//
//  def readOutput(): ProducerRecord[JsonNode, AdAnnouncementRuleClient] = {
//    testDriver.readOutput(adClientAnnouncementOutputTopic,
//      new JsonDeserializer(), valueSpecificAdAnnouncementRuleClientAvroSerde.deserializer())
//  }
//
//
////  test("test") {
////    val clientFactory = new ConsumerRecordFactory[String, AdClient](adClientTopic,
////      new StringSerializer,
////      valueSpecificAdClientAvroSerde.serializer())
////
////    val ruleFactory = new ConsumerRecordFactory[String, AdRule](adRuleTopic,
////      new StringSerializer,
////      valueSpecificAdRuleAvroSerde.serializer())
////
////    val adAnnouncementFactory = new ConsumerRecordFactory[String, AdAnnouncement](
////      adAnnouncementTopic,
////      new StringSerializer,
////      valueSpecificAdAnnouncementAvroSerde.serializer())
////
////    val adClient = AdClient(Option("100"), Option("Sasha"))
////
////    testDriver.pipeInput(clientFactory.create(adClientTopic, adClient.id.get, adClient))
////
////    val adRule = AdRule(
////      Option("rule0"),
////      Option("france rule"),
////      Option("10"),
////      Option(Rule(
////        Option(Seq("male", "female")),
////        Option(Seq("France")))))
////
////    testDriver.pipeInput(ruleFactory.create(adRuleTopic, adRule.id.get, adRule))
////
////    val adAnnouncement = AdAnnouncement(
////      Option("10"),
////      Option("ad-10"),
////      Option("some text"),
////      Option("science")
////    )
////
////    testDriver.pipeInput(adAnnouncementFactory.create(adAnnouncementTopic,
////      adAnnouncement.id.get,
////      adAnnouncement))
////
////    val strIdentifier = "{" + "\"" + "id" + "\"" + " : " + "\"" +  adClient.id.get +
////      "_" + adRule.id.get + "_" + adAnnouncement.id.get + "\"" + "}"
////    val mapper = new ObjectMapper()
////    val key: JsonNode = mapper.readTree(strIdentifier)
////
////    val resAdAnnouncementRuleClient = AdAnnouncementRuleClient(adRule.id, adClient.id,
////      adAnnouncement.id, adAnnouncement.name, adAnnouncement.text,
////      adAnnouncement.category, adAnnouncement.adPartnerId)
////
////    val output = readOutput()
////    println(output)
////
////    val output1 = readOutput()
////    println(output1)
////
////    val output2 = readOutput()
////    println(output2)
//////    println(output.key())
//////    println(output.value())
////
////    //OutputVerifier.compareKeyValue(readOutput(), key, resAdAnnouncementRuleClient)
////
////  }
//
//  after {
//    println("after")
//    testDriver.close()
//  }
//
//}
