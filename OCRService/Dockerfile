FROM gradle:9.5.1-jammy AS builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jdk AS base
WORKDIR /app

RUN apt-get update && \
        apt-get install -y --no-install-recommends \
            tesseract-ocr \
            tesseract-ocr-rus \
            tesseract-ocr-eng \
            tesseract-ocr-fra \
            libtesseract-dev \
            libleptonica-dev \
            libgomp1 \
        && rm -rf /var/lib/apt/lists/*

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

RUN ln -sf /usr/lib/libquadmath.so /usr/lib/libquadmath.so.0

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu

CMD java -XX:MaxRAMPercentage=70 \
         -XX:MinRAMPercentage=50 \
         -XX:InitialRAMPercentage=30 \
         -XX:+UseG1GC \
         -XX:+UseStringDeduplication \
         -XX:+ExitOnOutOfMemoryError \
         -Djava.security.egd=file:/dev/./urandom \
         -jar app.jar