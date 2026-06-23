package com.sumo.sumo_analyzer.mapper;

import com.sumo.sumo_analyzer.entity.TorikumiResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface TorikumiResultMapper {
    void insert(TorikumiResult record);

    int countByBashoAndDayAndRikishi(
            @Param("basho") String basho,
            @Param("day") Integer day,
            @Param("eastRikishiId") Integer eastRikishiId,
            @Param("westRikishiId") Integer westRikishiId);

    String findLatestBasho();
    List<Map<String, Object>> findWinningKimariteStats(@Param("rikishiId") Integer rikishiId);
    List<Map<String, Object>> findRivalStats(@Param("rikishiId") Integer rikishiId);

    // ★新規追加：全試合を「昔から順番に（場所→日→ID）」取得する
    List<TorikumiResult> findAllOrderByChronological();

    // ★新規追加：計算したEloレーティングをデータベースに更新（UPDATE）する
    void updateElo(TorikumiResult record);

    // ★新規追加：指定した力士のEloレーティング（戦闘力）の推移を時系列で取得
    List<Map<String, Object>> findEloHistory(@Param("rikishiId") Integer rikishiId);
}