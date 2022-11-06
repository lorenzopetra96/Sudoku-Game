FROM alpine/git as git
WORKDIR /last-version
RUN git clone https://github.com/lorenzopetra96/sudoku-game.git

FROM maven:3.8.4-openjdk-17 as mvn
WORKDIR /maven-jar
COPY --from=git /last-version/sudoku-game /maven-jar
RUN mvn package

FROM openjdk
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=mvn /maven-jar/target/sudoku-game-1.0-jar-with-dependencies.jar /app

CMD /usr/bin/java -jar sudoku-game-1.0-jar-with-dependencies.jar -m $MASTERIP -id $ID