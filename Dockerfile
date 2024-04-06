FROM openjdk:17-alpine

WORKDIR /phobot
COPY . /phobot

RUN mkdir -p run/client/mods
RUN wget -O run/client/mods/hmc-specifics-1.20.4.jar \
    https://github.com/3arthqu4ke/hmc-specifics/releases/download/v1.20.4-1.8.1/hmc-specifics-fabric-1.20.4-1.8.1.jar

RUN wget -O run/client/mods/hmc-optimizations-0.3.0-fabric.jar \
    https://github.com/3arthqu4ke/hmc-optimizations/releases/download/latest/hmc-optimizations-0.3.0-fabric.jar

RUN mkdir -p run/client/pingbypass/server
RUN cp -r -f config/binds/ run/client/pingbypass/server/
RUN cp -r -f config/settings/ run/client/pingbypass/server/
RUN cp -f config/options.txt run/client/

RUN chmod +x gradlew
RUN ./gradlew -Phmc.lwjgl=true -Ppb.server=true test
RUN ./gradlew -Phmc.lwjgl=true -Ppb.server=true fabricJar
RUN ./gradlew -Phmc.lwjgl=true -Ppb.server=true fabricPreRunClient
ENTRYPOINT sh -c "./gradlew -Phmc.lwjgl=true -Ppb.server=true fabricRunClient --console=plain"
