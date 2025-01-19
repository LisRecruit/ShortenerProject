FROM openjdk:21-slim
MAINTAINER shortUrl.com
COPY build/libs/ShortenerProject-0.0.1-SNAPSHOT.jar ShortenerProject.jar
ENTRYPOINT ["java", "-jar", "/ShortenerProject.jar"]
