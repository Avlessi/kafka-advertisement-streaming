package com.avlesi.advertisement

import com.avlesi.advertisement.model._
import com.typesafe.config.{Config, ConfigFactory}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import com.avlesi.advertisement.producers.AdProducer
import com.avlesi.advertisement.streaming.ClientViewStreaming
import java.io.{BufferedReader, InputStreamReader}
import java.util.{Collections, Properties, UUID}
import scala.concurrent.ExecutionContext.global

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.mongodb.scala._

@RunWith(classOf[JUnitRunner])
class ClientViewStreamingIntegrationTest extends FunSuite with BeforeAndAfter {

  val config: Config = ConfigFactory.parseResources("test-application.conf")
  val adProducer = new AdProducer(config)
  val adTopic: String = config.getConfig("ad-announcement").getString("topic")
  val adRuleTopic: String = config.getConfig("ad-rule").getString("topic")
  val adClientTopic: String = config.getConfig("ad-client").getString("topic")
  val adClientAnnouncementOutputTopic: String = config.getConfig("output").getString("topic")
  val mongoClient: MongoClient = MongoClient()
  val mongoDbName: String = config.getConfig("mongo").getString("db")
  val mongoCollectionName: String = config.getConfig("mongo").getString("collection")
  val mongoCollection = mongoClient.getDatabase(mongoDbName).getCollection(mongoCollectionName)

  before {
    // recreate topics
    val processBuilder = new ProcessBuilder("/bin/bash", config.getConfig("script").
      getString("recreateTopics"))

    val process = processBuilder.start()
    val reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
    var output: String = ""
    while ({
      output = reader.readLine();  output!= null}) {
      println(output)
    }

    // drop mongo collection
    val futureDrop = mongoCollection.drop().toFuture()
    futureDrop.onComplete(dropEvent => {
      println("mongo collection is dropped")
    })(scala.concurrent.ExecutionContext.global)

    //toFuture method comes from ObservableImplicits class
  }

  test("test client announcement view") {

    val streaming = new ClientViewStreaming(config)
    val thread = new Thread(new Runnable {
      override def run()= {
        streaming.startStreaming()
      }
    })
    thread.start()

    // give stream app time to start
    Thread.sleep(7000)


    val adAnnouncement = AdAnnouncement(
      "announce10",
      Option(AnnouncementDetails(
        Option("ad-10"),
        Option("some text"),
        Option("science")
    )))

    val adAnnouncement1 = AdAnnouncement(
      "announce11",
      Option(AnnouncementDetails(
        Option("ad-11"),
        Option("some text"),
        Option("biology")
      )))
    adProducer.send(adAnnouncement.id, adAnnouncement, adTopic).get()
    adProducer.send(adAnnouncement1.id, adAnnouncement1, adTopic).get()

    val rule: Rule = Rule(Option(List("male", "female")),
      Option(List(15, 50)),
      Option(List("France", "Germany")),
      Option(List("Paris", "Berlin")))

    val adRule = AdRule(
      "rule0",
      Option("france rule"),
      Option("announce10"),
      Option(rule))

    val adRule1 = AdRule(
      "rule1",
      Option("france rule"),
      Option("announce11"),
      Option(rule))

    adProducer.send(adRule.id, adRule, adRuleTopic).get()
    adProducer.send(adRule1.id, adRule1, adRuleTopic).get()

    val adGoodClient = AdClient("client0", Option(ClientDetails(Option("Mika"), Option("female"), Option(19),
      Option("France"), Option("Paris"))))

    val adBadClient = AdClient("client1", Option(ClientDetails(Option("Darwin"), Option("male"), Option(20),
      Option("France"), Option("Nantes"))))

    val adNiceClient = AdClient("client2", Option(ClientDetails(Option("Nancy"), Option("female"), Option(25),
      Option("France"), Option("Paris"))))

    adProducer.send(adGoodClient.id, adGoodClient, adClientTopic).get()
    adProducer.send(adBadClient.id, adBadClient, adClientTopic).get()
    adProducer.send(adNiceClient.id, adNiceClient, adClientTopic).get()

    Thread.sleep(10000)

    val expectedObjectsNumberInOutput = 4

    val futureCountDocuments = mongoCollection.countDocuments().toFuture()
    futureCountDocuments.onComplete(count => {
      assert(count.get === expectedObjectsNumberInOutput)
    })(scala.concurrent.ExecutionContext.global)

    println()
    println("-------------------")
    println("documents in mongo")
    println("-------------------")
    mongoCollection.find().foreach(doc => {
      println(doc)
    })
  }

  after {
    mongoClient.close()
  }


}
