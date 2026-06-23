package com.sumo.sumo_analyzer.mapper;

import com.sumo.sumo_analyzer.entity.RikishiBashoRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RikishiBashoRecordMapper {
    void insert(RikishiBashoRecord record);

    // 同じ力士の同じ場所のデータが二重登録されないようにするチェック
    int countByRikishiIdAndBasho(@Param("rikishiId") Integer rikishiId, @Param("basho") String basho);

    // 既存の countByRikishiIdAndBasho などの下に追加
    List<RikishiBashoRecord> findByRikishiId(Integer rikishiId);
}