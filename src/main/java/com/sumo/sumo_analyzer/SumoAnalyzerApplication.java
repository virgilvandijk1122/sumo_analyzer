package com.sumo.sumo_analyzer;

import com.sumo.sumo_analyzer.service.SumoScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SumoAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SumoAnalyzerApplication.class, args);
	}

	// アプリケーション起動直後に実行されるテスト用メソッド
//	@Bean
//	public CommandLineRunner runScraper(SumoScraperService scraperService) {
//		return args -> {
//			System.out.println("=== Spring Boot アプリ起動完了 ===");
//			// ここでスクレイピング処理を呼び出す
//			scraperService.testScrape();
//			System.out.println("==================================");
//		};
//	}
}
