const { Kafka } = require('kafkajs');

const broker = process.env.KAFKA_BROKER || 'localhost:9092';

const kafka = new Kafka({
  clientId: 'node-consumer',
  brokers: [broker]
});

const consumer = kafka.consumer({ groupId: 'node-group' });

const run = async () => {
  await consumer.connect();
  console.log('🟩 Node.js Consumer connected to Kafka!');
  
  await consumer.subscribe({ topic: 'user-events', fromBeginning: true });
  
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      console.log(`🚀 Node Consumer Received message -> ${message.value.toString()}`);
    },
  });
};

run().catch(console.error);
