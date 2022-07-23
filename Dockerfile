FROM hseeberger/scala-sbt:graalvm-ce-20.0.0-java11_1.4.3_2.13.3 as builder
# https://vyshane.com/2018/11/27/multistage-docker-builds-scala/

WORKDIR /build

COPY project project
COPY build.sbt .
RUN sbt update

COPY . .
RUN sbt stage


FROM openjdk:11-jre-slim-stretch
WORKDIR /app
COPY --from=builder /build/target/universal/stage/. .
RUN mv bin/$(ls bin | grep -v .bat) bin/start
CMD ["./bin/start"]
