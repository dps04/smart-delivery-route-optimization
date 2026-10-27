FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p build/classes && javac -d build/classes src/main/java/com/logismart/*.java && jar --create --file app.jar --main-class com.logismart.App -C build/classes .

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar ./app.jar
COPY data ./data
EXPOSE 8080
ENV PORT=8080
CMD ["sh", "-c", "java -Dport=${PORT} -Ddata.dir=data -jar app.jar"]
