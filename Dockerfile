FROM tojatos/scala-sbt:8u222_1.3.5_2.13.1 as builder

# https://vyshane.com/2018/11/27/multistage-docker-builds-scala/

WORKDIR /build

COPY project project
COPY build.sbt .
RUN sbt update

COPY . .
RUN sbt stage


FROM openjdk:8u222-jre-slim
WORKDIR /app
COPY --from=builder /build/target/universal/stage/. .
RUN mv bin/$(ls bin | grep -v .bat) bin/start
CMD ["./bin/start"]
