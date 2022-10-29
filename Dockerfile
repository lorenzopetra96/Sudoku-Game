FROM alpine/git
WORKDIR /project
RUN git clone https://github.com/lorenzopetra96/sudoku-game.git

FROM maven:3.5-jdk-8-alpine
WORKDIR /project
COPY --from=0 /project/sudoku-game /project
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=1 /project/target/sudoku-game-1.0-jar-with-dependencies.jar /app

CMD /usr/bin/java -jar sudoku-game-1.0-jar-with-dependencies.jar -m $MASTERIP -id $ID
