# 1. ビルド環境の準備 (Java 21)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# プロジェクトのファイルをすべてコピー
COPY . .

# ★最終兵器：Windows特有の改行コード(CRLF)をLinux用(LF)に変換し、実行権限を付与する
RUN apt-get update && apt-get install -y dos2unix
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew

# Gradleを使ってアプリケーションをビルド (jarファイルを作成)
RUN ./gradlew bootJar --no-daemon

# 2. 本番実行環境の準備 (軽量なJava 21)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# ビルドしたjarファイルをコピー
COPY --from=build /app/build/libs/*.jar app.jar

# Webサーバーのポートを公開
EXPOSE 8080

# アプリケーションの起動
ENTRYPOINT ["java", "-jar", "app.jar"]