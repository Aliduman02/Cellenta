#!/bin/bash

# CHF AGGRESSIVE Performance Startup Script
# Java 17 Optimized for High TPS

APP_NAME="CHF (Charging Function)"
JAR_FILE="src/main/resources/chf-1.0.0-SNAPSHOT.jar"
LOG_FILE="chf-startup.log"
PID_FILE="chf.pid"

# ⚡ AGGRESSIVE JVM Settings - Max Performance
JVM_OPTS="
    -server
    -Xms512m
    -Xmx1g
    -XX:NewRatio=1
    -XX:SurvivorRatio=8
    -XX:+UseG1GC
    -XX:G1HeapRegionSize=4m
    -XX:G1ReservePercent=10
    -XX:MaxGCPauseMillis=30
    -XX:+UseStringDeduplication
    -XX:+UseCompressedOops
    -XX:+UseCompressedClassPointers
    -XX:+TieredCompilation
    -XX:TieredStopAtLevel=1
    -Djava.awt.headless=true
    -Duser.timezone=Europe/Istanbul
    -Dfile.encoding=UTF-8
    -Djava.security.egd=file:/dev/./urandom
    -Dnetworkaddress.cache.ttl=5
    -Dnetworkaddress.cache.negative.ttl=1
    -Dio.netty.leakDetection.level=disabled
    -Dio.netty.recycler.maxCapacityPerThread=0
    -Dio.netty.allocator.numDirectArenas=0
    -Dreactor.netty.ioWorkerCount=4
    -Dreactor.netty.ioSelectCount=1
    -Dspring.profiles.active=production
    -Dspring.jmx.enabled=false
    -Dlogging.level.root=ERROR
"

# ⚡ MINIMAL Monitoring
MONITORING_OPTS="
    -Xlog:gc*:gc.log:time,tags:filecount=2,filesize=10M
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=./heapdumps/
"

# Network optimizations
NETWORK_OPTS="
    -Djava.net.preferIPv4Stack=true
    -Dsun.net.useExclusiveBind=false
    -Djava.net.preferIPv6Addresses=false
"

# Create heap dump directory
mkdir -p heapdumps

echo "=========================================="
echo "🚀 Starting $APP_NAME - AGGRESSIVE MODE"
echo "=========================================="
echo "📅 Date: $(date '+%Y-%m-%d %H:%M:%S')"
echo "🏠 Working Directory: $(pwd)"
echo "📦 JAR File: $JAR_FILE"
echo "💾 JVM Memory: 512MB-1GB Heap (AGGRESSIVE)"
echo "🔧 GC: G1 with 30ms pause target"
echo "🌍 Timezone: Europe/Istanbul"
echo "☕ Java Version: $(java -version 2>&1 | head -n 1)"
echo "⚡ Mode: HIGH PERFORMANCE - LOW LATENCY"
echo "=========================================="

# Check JAR file
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "🔨 Running Maven build..."
    mvn clean package -DskipTests -q
    
    if [ -f "target/chf-1.0.0-SNAPSHOT.jar" ]; then
        echo "📦 Copying JAR to resources..."
        cp target/chf-1.0.0-SNAPSHOT.jar src/main/resources/
        rm -rf target/
    else
        echo "❌ Build failed! JAR file still not found."
        exit 1
    fi
fi

# Stop previous instance
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo "⚠️  Stopping previous CHF instance (PID: $OLD_PID)..."
        kill -TERM $OLD_PID
        sleep 3
        
        if ps -p $OLD_PID > /dev/null 2>&1; then
            echo "🔥 Force killing previous instance..."
            kill -9 $OLD_PID
        fi
    fi
    rm -f "$PID_FILE"
fi

# Start with AGGRESSIVE settings
echo "🎯 Starting CHF with AGGRESSIVE performance settings..."
echo "📊 Monitoring: Minimal GC logging"
echo "🌙 Background mode enabled"
echo "⏳ Starting..."

nohup java $JVM_OPTS $MONITORING_OPTS $NETWORK_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
APP_PID=$!

# Save PID
echo $APP_PID > "$PID_FILE"

# Startup check
sleep 3
if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✅ CHF started successfully!"
    echo "🆔 Process ID: $APP_PID"
    echo "📝 Log file: $LOG_FILE"
    echo "📊 Monitor with: tail -f $LOG_FILE"
    echo "🛑 Stop with: kill $APP_PID"
    echo ""
    echo "🌐 Service endpoints:"
    echo "   ❤️  Health: curl http://localhost:8080/chf/health"
    echo "   📈 TPS: curl http://localhost:8080/chf/tps/summary"
    echo "   🧪 Test: curl http://localhost:8080/chf/test/ping"
    echo ""
    echo "⚡ AGGRESSIVE MODE FEATURES:"
    echo "   • VoltDB timeout: 400ms"
    echo "   • Memory: 512MB-1GB"
    echo "   • GC pause: 30ms target"
    echo "   • Minimal logging"
    echo "   • Aggressive connection pooling"
    echo ""
    
    # Show initial logs for 5 seconds
    echo "📋 Initial logs (first 5 seconds):"
    timeout 5 tail -f "$LOG_FILE" 2>/dev/null || true
    
else
    echo "❌ Failed to start CHF!"
    echo "📝 Check logs: tail -f $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi

echo ""
echo "🎉 CHF AGGRESSIVE MODE is running!"
echo "🔥 Optimized for maximum TPS and minimum latency!"
echo "📈 Expected performance: 30-50 TPS, VoltDB calls <100ms"
