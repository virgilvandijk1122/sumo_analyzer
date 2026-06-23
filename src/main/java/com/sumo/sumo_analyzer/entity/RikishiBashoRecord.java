package com.sumo.sumo_analyzer.entity;

import lombok.Data;

@Data
public class RikishiBashoRecord {
    private Integer id;
    private Integer rikishiId;       // RikishiテーブルのIDと紐づく外部キー
    private String basho;            // 例: "202401" (2024年1月場所)
    private String banzuke;          // 例: "東横綱", "西前頭3"
    private Integer banzukeRank;     // 数値化した番付（1=横綱, 2=大関... 差分計算用）
    private String highestBanzuke;
    private Integer wins = 0;        // 勝数
    private Integer losses = 0;      // 負数
    private Integer absences = 0;    // 休日数
    private Boolean isKadoban = false;
    private Boolean isKyujoAke = false;
    private Integer yushoCount = 0;
    private Integer sanshoCount = 0;
    private Integer kinboshiCount = 0;
}
