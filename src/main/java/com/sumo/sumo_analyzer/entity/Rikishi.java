package com.sumo.sumo_analyzer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data // これをつけるだけで、ゲッター・セッター・toStringなどを自動生成してくれます
public class Rikishi {
    private Integer id;
    private String shikona;
//    -- 四股名
    private String heya;
//    -- 部屋
    private String shusshin;
//    -- 出身地
    private LocalDate birthDate;
//    -- 生年月日
    private String hatsuBasho;
//    -- 初土俵
    private BigDecimal height;
//    -- 身長(cm) ※SQLのNUMERICに対応
    private BigDecimal weight;
//    -- 体重(kg) ※SQLのNUMERICに対応
    private String tokuiGata;
//    -- 得意な型

    // 【新規追加】計算したBMIを一時的に保持するための変数
    private Double bmi;
}
