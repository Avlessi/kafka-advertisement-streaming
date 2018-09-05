package com.avlesi.advertisement.producers

import java.util.{Properties, UUID}

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

class AdProducer(config: Config) {

  private def createProducerProps(conf: Config): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getString("bootstrap.servers"))
    props.put(ProducerConfig.ACKS_CONFIG, conf.getString("acks"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, conf.getString("client_id") + UUID.randomUUID())
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, conf.getString("key.serializer"))
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, conf.getString("value.serializer"))
    props.put("schema.registry.url", conf.getString("schema.registry.url"))
    props
  }

  def send[K,V](key: K, value: V, topic: String) = {
    val producerConf = config.getConfig("kafka-producer")
    val producerProps = createProducerProps(producerConf)
    val producer = new KafkaProducer[K, V](producerProps)
    val producerRecord = new ProducerRecord[K, V](topic,key, value)
    producer.send(producerRecord)
  }
}
