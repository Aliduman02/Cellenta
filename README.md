# Kafka Router Application

Modern, production-ready Kafka consumer/producer application with enterprise-grade features.

## 🚀 Features

- **Generic Message Handling**: Type-safe serialization/deserialization
- **Graceful Shutdown**: Thread-safe resource cleanup
- **Health Monitoring**: Real-time application health checks
- **Retry Mechanism**: Exponential backoff for failed operations
- **Circuit Breaker**: Fault tolerance pattern implementation
- **Metrics Collection**: Comprehensive performance monitoring
- **Logging**: Structured logging with Logback
- **Configuration Validation**: Environment-aware configuration
- **Test Mode**: Optional message generation for development

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Kafka Cluster (tested with 34.38.128.100:9092,9093,9094)

## 🏗️ Architecture

```
src/main/java/com/kafka/
├── Main.java                    # Application entry point
├── ConfigLoader.java           # Configuration management
├── KafkaTopicConstants.java    # Topic definitions
├── MessageConsumer.java        # Generic consumer
├── MessageProducer.java        # Generic producer
├── config/
│   └── ConfigValidator.java    # Configuration validation
├── health/
│   └── HealthChecker.java      # Health monitoring
├── metrics/
│   └── MetricsCollector.java   # Performance metrics
├── retry/
│   └── RetryHandler.java       # Retry mechanism
├── circuit/
│   └── CircuitBreaker.java     # Circuit breaker pattern
├── message/                    # Message models
│   ├── Message.java
│   ├── BalanceMessage.java
│   ├── NotificationMessage.java
│   └── UsageRecordMessage.java
├── serializer/                 # Message serializers
│   ├── GenericMessageSerializer.java
│   ├── BalanceMessageSerializer.java
│   ├── NotificationMessageSerializer.java
│   └── UsageRecordMessageSerializer.java
└── deserializer/              # Message deserializers
    ├── GenericMessageDeserializer.java
    ├── BalanceMessageDeserializer.java
    ├── NotificationMessageDeserializer.java
    └── UsageRecordMessageDeserializer.java
```

## 🚀 Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Run in Production Mode

```bash
java -jar target/KafkaApp-1.0-SNAPSHOT.jar
```

### 3. Run in Test Mode (Generate Test Messages)

```bash
# Using environment variable
$env:test_messages="true"; java -jar target/KafkaApp-1.0-SNAPSHOT.jar

# Using system property
java -Dtest.messages=true -jar target/KafkaApp-1.0-SNAPSHOT.jar
```

## ⚙️ Configuration

### Environment Variables

- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers
- `test_messages`: Enable test mode (any value)

### Configuration Files

- `src/main/resources/kafka-config.properties`: Kafka configuration
- `src/main/resources/application.properties`: Application settings
- `src/main/resources/logback.xml`: Logging configuration

## 📊 Monitoring

### Health Check

The application provides real-time health monitoring:

- Message processing statistics
- Success/failure rates
- Uptime tracking
- Error counts by type

### Metrics

Comprehensive metrics collection:

- Total messages processed/sent/received
- Topic-specific message counts
- Error tracking
- Performance indicators

### Logging

Structured logging with Logback:

- Console output for development
- File logging for production
- Configurable log levels
- Rolling file policy

## 🔧 Best Practices Implemented

### 1. **Resource Management**
- Try-with-resources for automatic cleanup
- Graceful shutdown with JVM hooks
- Thread-safe operations

### 2. **Error Handling**
- Comprehensive exception handling
- Retry mechanism with exponential backoff
- Circuit breaker pattern for fault tolerance

### 3. **Configuration**
- Environment variable support
- Configuration validation
- Fallback defaults

### 4. **Monitoring**
- Health checks
- Metrics collection
- Structured logging

### 5. **Code Quality**
- Generic design patterns
- Type safety
- Clean architecture
- Separation of concerns

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Testing

The application includes a test mode that generates sample messages for integration testing.

## 📈 Performance

- **High Throughput**: Optimized for high message volumes
- **Low Latency**: Efficient serialization/deserialization
- **Memory Efficient**: Proper resource management
- **Scalable**: Thread-safe design

## 🔒 Security

- Configuration validation
- Input sanitization
- Secure resource handling

## 🚨 Troubleshooting

### Common Issues

1. **Connection Errors**: Verify Kafka cluster is running
2. **Serialization Errors**: Check message format compatibility
3. **Memory Issues**: Monitor heap usage

### Logs

Check application logs for detailed error information:

```bash
tail -f logs/kafka-app.log
```

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For support and questions, please contact the development team. 