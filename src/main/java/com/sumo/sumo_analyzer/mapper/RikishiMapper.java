package com.sumo.sumo_analyzer.mapper;

import com.sumo.sumo_analyzer.entity.Rikishi;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper // Spring BootにMyBatisのMapperであることを教える
public interface RikishiMapper {

    // 今回スクレイピングで取れる4つの項目をINSERTするSQL
    void insert(Rikishi rikishi);

    // 登録されている力士をID順で全件取得する
    List<Rikishi> findAll();

    // 【新規追加】四股名でDBに存在するかチェックするためのメソッド（重複チェック用メソッドの追加）
    int countByShikona(@Param("shikona") String shikona);

    // 既存の countByShikona の下に追加
    Integer findIdByShikona(@Param("shikona") String shikona);

    // 既存の countByShikona などの下に追加
    Rikishi findById(Integer id);

    List<Rikishi> findByCondition(@Param("heya") String heya);
    List<String> findDistinctHeya();
}
