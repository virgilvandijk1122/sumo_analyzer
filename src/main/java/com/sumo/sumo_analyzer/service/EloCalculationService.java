package com.sumo.sumo_analyzer.service;

import com.sumo.sumo_analyzer.entity.TorikumiResult;
import com.sumo.sumo_analyzer.mapper.TorikumiResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EloCalculationService {

    private final TorikumiResultMapper torikumiResultMapper;

    public void calculateAllElo() {
        System.out.println("=== Eloレーティング（戦闘力）の全自動シミュレーションを開始します ===");

        // 1. 全試合データを昔から順番に取得する
        List<TorikumiResult> allMatches = torikumiResultMapper.findAllOrderByChronological();
        System.out.println("取得した全試合数: " + allMatches.size() + " 件");

        // 2. 力士ごとの「現在の戦闘力」を記憶しておく脳みそ（Map）
        Map<Integer, Double> eloMap = new HashMap<>();

        int count = 0;

        // 3. 第1試合から順番にシミュレーション開始！
        for (TorikumiResult match : allMatches) {
            Integer eastId = match.getEastRikishiId();
            Integer westId = match.getWestRikishiId();
            Integer winnerId = match.getWinnerRikishiId();

            if (winnerId == null) continue; // 勝敗不明なデータはスキップ

            // ★ 新人は1500からスタート、既存の力士は現在の戦闘力を取り出す
            double eastElo = eloMap.getOrDefault(eastId, 1500.0);
            double westElo = eloMap.getOrDefault(westId, 1500.0);

            // ★ 試合前の戦闘力をデータベース保存用にセット
            match.setEastEloBefore(eastElo);
            match.setWestEloBefore(westElo);
            torikumiResultMapper.updateElo(match); // データベースを上書き！

            // ----------------------------------------------------
            // 🧠 ここからEloレーティングの数学的アルゴリズム（AIの脳）
            // ----------------------------------------------------

            // ① 両者の「勝率の予測値（0.0〜1.0）」を計算
            double expectedEast = 1.0 / (1.0 + Math.pow(10.0, (westElo - eastElo) / 400.0));
            double expectedWest = 1.0 / (1.0 + Math.pow(10.0, (eastElo - westElo) / 400.0));

            // ② 実際の勝敗（勝ったら1.0、負けたら0.0）
            double scoreEast = winnerId.equals(eastId) ? 1.0 : 0.0;
            double scoreWest = winnerId.equals(westId) ? 1.0 : 0.0;

            // ③ 変動値（Kファクターは一般的な「32」を使用）
            int K = 32;
            double newEastElo = eastElo + K * (scoreEast - expectedEast);
            double newWestElo = westElo + K * (scoreWest - expectedWest);

            // ④ 計算し終わった新しい戦闘力を脳みそに上書きして、次の試合へ！
            eloMap.put(eastId, newEastElo);
            eloMap.put(westId, newWestElo);

            count++;
            if (count % 1000 == 0) {
                System.out.println(count + " 件のシミュレーション完了...");
            }
        }

        System.out.println("=== 🏆 全シミュレーション完了！データベースの更新に成功しました！ ===");
    }
}