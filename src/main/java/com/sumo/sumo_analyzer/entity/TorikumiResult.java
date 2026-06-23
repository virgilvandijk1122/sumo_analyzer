package com.sumo.sumo_analyzer.entity;

import lombok.Data;

@Data
public class TorikumiResult {
    private Integer id;
    private String basho;
    private Integer day;
    private Integer eastRikishiId;
    private Integer westRikishiId;
    private Integer winnerRikishiId;
    private String kimarite;

    // ★新規追加：試合直前のEloレーティング（戦闘力）
    private Double eastEloBefore;
    private Double westEloBefore;
}