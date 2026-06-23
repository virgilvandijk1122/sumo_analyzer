# 1. ビルド環境の準備 (Java 21)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# プロジェクトのファイルをすべてコピー
COPY . .

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
