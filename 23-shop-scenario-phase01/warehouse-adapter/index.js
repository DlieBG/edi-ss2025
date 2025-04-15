// Imports
const { Kafka, Partitioners } = require('kafkajs');
const mqtt = require('mqtt');

const kafka = new Kafka({
    clientId: 'warehouse-adapter',
    brokers: ['localhost:9092'],
});
const kafka_consumer = kafka.consumer({
    groupId: 'warehouse-adapter',
});
const kafka_producer = kafka.producer({
    createPartitioner: Partitioners.DefaultPartitioner,
});

const mqtt_client = mqtt.connect('mqtt://localhost:1883');

mqtt_client.on('connect', () => {
    mqtt_client.subscribe('article/+/stock');
});

// receive mqtt 'stock' and publish to kafka 'item-stock'
mqtt_client.on('message', (topic, message) => {
    const id = /^article\/(.*?)\/stock$/.exec(topic)[1];
    const stock = parseInt(message);

    kafka_producer.send({
        topic: 'stock-update',
        messages: [
            { value: JSON.stringify({ articleId: id, stock }) },
        ],
    });
});

// receive kafka 'items-pick' and publish to mqtt 'picked'
const run = async () => {
    await kafka_producer.connect();

    await kafka_consumer.connect();
    await kafka_consumer.subscribe({ topic: 'items-pick', fromBeginning: true });

    await kafka_consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            const dto = JSON.parse(message.value.toString());

            dto.forEach(item => {
                mqtt_client.publish(`article/${item.articleId}/picked`, `${item.count}`);
            });
        },
    });
};

run().catch(console.error);
