package com.avlesi.advertisement.streaming

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.{Collections, Properties, UUID}

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import com.avlesi.advertisement.model._
import com.avlesi.advertisement.serde.ListStringSerde
import com.avlesi.advertisement.util.ConstraintChecker
import com.lightbend.kafka.scala.streams._
import com.lightbend.kafka.scala.streams.DefaultSerdes._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams._
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.streams.kstream._
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer

class ClientViewStreaming(config: Config) {

  //val config: Config = ConfigFactory.parseResources("application.conf")
  val specialRuleKey = "rule"

  def createStreamsConf: Properties = {
    val props = new Properties()
    val kafkaConf = config.getConfig("kafka")
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaConf.getString("client_id")
      + "-" + UUID.randomUUID())
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConf.getString("bootstrap.servers"))
    props.put("schema.registry.url", kafkaConf.getString("schema.registry.url"))
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    props
  }

  def createTopology() = {

    val adClientTopic = config.getConfig("ad-client").getString("topic")
    val adRuleTopic = config.getConfig("ad-rule").getString("topic")
    val adAnnouncementTopic = config.getConfig("ad-announcement").getString("topic")
    val adClientAnnouncementOutputTopic = config.getConfig("output").getString("topic")

    val serdeConfig = Collections.singletonMap("schema.registry.url",
    config.getConfig("kafka").getString("schema.registry.url"))

    // avro serde
    val valueSpecificAdClientAvroSerde = new SpecificAvroSerde[AdClient]
    valueSpecificAdClientAvroSerde.configure(serdeConfig, false) // `false` for record values

    val valueSpecificAdRuleAvroSerde = new SpecificAvroSerde[AdRule]
    valueSpecificAdRuleAvroSerde.configure(serdeConfig, false)

    val valueSpecificAdAnnouncementAvroSerde = new SpecificAvroSerde[AdAnnouncement]
    valueSpecificAdAnnouncementAvroSerde.configure(serdeConfig, false)

    val valueSpecificAdRuleClientAvroSerde = new SpecificAvroSerde[AdRuleClient]
    valueSpecificAdRuleClientAvroSerde.configure(serdeConfig, false)

    val valueSpecificAdAnnouncementRuleClientAvroSerde = new SpecificAvroSerde[AdAnnouncementRuleClient]
    valueSpecificAdAnnouncementRuleClientAvroSerde.configure(serdeConfig, false)

    val builder = new StreamsBuilderS

    //get table containing the latest data of user
    val clientTable: KTableS[String, AdClient] = builder.table[String, AdClient](adClientTopic)(Consumed.
      `with`(stringSerde, valueSpecificAdClientAvroSerde))

    //get table containing the latest data of specific rule
    val ruleTable: KTableS[String, AdRule] = builder.table[String, AdRule](adRuleTopic)(Consumed.
      `with`(stringSerde, valueSpecificAdRuleAvroSerde))

    //get table containing the latest data of specific ad announcement
    val adTable: KTableS[String, AdAnnouncement] =
      builder.table[String, AdAnnouncement](adAnnouncementTopic)(Consumed.
      `with`(stringSerde, valueSpecificAdAnnouncementAvroSerde))

    val clientStream: KStreamS[String, AdClient] = clientTable.toStream
    val ruleStream: KStreamS[String, AdRule] = ruleTable.toStream


    ////// block making Cartesian product between clients and rules
    ////// client filtering according rules will happen further

    val groupedRuleStream: KGroupedStreamS[String, String] = ruleStream.
      map((k, v) => (specialRuleKey, v.id)).
      groupByKey(Serialized.`with`(stringSerde, stringSerde))

    // aggregate to a list of rules
    val ruleListTable: KTableS[String, List[String]] = groupedRuleStream.aggregate(
          () => List[String](),
          (aggKey: String, newValue: String, aggValue: List[String]) => newValue :: aggValue,
          Materialized.`with`(stringSerde, new ListStringSerde)
        )

    val specialRuleKeyClientStream: KStreamS[String, AdClient] =
      clientStream.map((k, v) => (specialRuleKey, v))

    val clientRulesStream: KStreamS[String, (AdClient, List[String])] = specialRuleKeyClientStream.
      join(ruleListTable,
      (client: AdClient, rules: List[String]) => (client, rules))(Joined.
      `with`(stringSerde, valueSpecificAdClientAvroSerde, new ListStringSerde))


    val cleanedClientRulesStream: KStreamS[AdClient, List[String]] = clientRulesStream.
      map((_, v: (AdClient, List[String])) => (v._1, v._2))

    val flatClientRuleStream: KStreamS[AdClient, String] = cleanedClientRulesStream.flatMapValues(a => a)

    val ruleClientStream: KStreamS[String, AdClient] = flatClientRuleStream.map((k, v) => (v, k))

    //////////// end Cartesian product block

    val adRuleClientStream: KStreamS[String, AdRuleClient] = ruleClientStream.
      join(ruleTable, (adClient: AdClient, adRule: AdRule) => {

          AdRuleClient(adRule.id, adClient.id, adRule.adAnnouncementId,
            adRule.rule, adClient.clientDetails)

      })(Joined.`with`(stringSerde, valueSpecificAdClientAvroSerde, valueSpecificAdRuleAvroSerde))

    // rule filter
    val adRuleClientStreamFiltered: KStreamS[String, AdRuleClient] =
      adRuleClientStream.filter((k: String, v: AdRuleClient) => {
        ConstraintChecker.isRuleSatisfy(v.clientDetails, v.rule)
      })

    val adAnnounceKeyRuleClientStream: KStreamS[String, AdRuleClient] = adRuleClientStreamFiltered.
      map((k: String, v: AdRuleClient) => {
      (v.adAnnouncementId.get, v)
    })

    val adAnnouncementRuleClientStream: KStreamS[String, AdAnnouncementRuleClient] =
      adAnnounceKeyRuleClientStream.
      join(adTable, (adRuleClient: AdRuleClient, adAnnouncement: AdAnnouncement) => {
        val announcementDetails = adAnnouncement.announcementDetails
        AdAnnouncementRuleClient(
          adRuleClient.ruleId,
          adRuleClient.clientId,
          adAnnouncement.id,
          if (announcementDetails.isDefined) announcementDetails.get.name else None,
          if (announcementDetails.isDefined) announcementDetails.get.text else None,
          if (announcementDetails.isDefined) announcementDetails.get.category else None,
          if (announcementDetails.isDefined) announcementDetails.get.adPartnerId else None)
      })(Joined.`with`(stringSerde, valueSpecificAdRuleClientAvroSerde, valueSpecificAdAnnouncementAvroSerde))


    // use json serializer for key. We do it for mongodb connector which
    // fails if avro serializer is used
    val jsonSerializer = new JsonSerializer()
    val jsonDeserializer = new JsonDeserializer()
    val jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer)

    val resClientIdAnnouncementStream: KStreamS[JsonNode, AdAnnouncementRuleClient] =
      adAnnouncementRuleClientStream.map((k, v) => {
        val identifier = "{" + "\"" + "id" + "\"" + " : " + "\"" +  v.clientId +
          "_" + v.ruleId + "_" + v.adAnnouncementId + "\"" + "}"
        // TODO construct json node directly
        val mapper = new ObjectMapper()
        val obj: JsonNode = mapper.readTree(identifier)
        (obj, v)
      })

    resClientIdAnnouncementStream.foreach((k: JsonNode, v: AdAnnouncementRuleClient) => {
      println("id = " + k + " AdAnnouncementRuleClient = " + v)
    })

    resClientIdAnnouncementStream.to(adClientAnnouncementOutputTopic)(Produced.
      `with`(jsonSerde, valueSpecificAdAnnouncementRuleClientAvroSerde))

    builder.build()
  }

  def startStreaming() {
    val streams = new KafkaStreams(createTopology(), createStreamsConf)

    streams.cleanUp()
    streams.start()

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        streams.close()
      }
    }))
  }


  def main(args: Array[String]): Unit = {
    val streaming = new ClientViewStreaming(ConfigFactory.parseResources("application.conf"))
    streaming.startStreaming()
  }

}
