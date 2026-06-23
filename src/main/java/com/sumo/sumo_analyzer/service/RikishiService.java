package com.sumo.sumo_analyzer.service;

import com.sumo.sumo_analyzer.entity.Rikishi;
import com.sumo.sumo_analyzer.entity.RikishiBashoRecord;
import com.sumo.sumo_analyzer.mapper.RikishiBashoRecordMapper;
import com.sumo.sumo_analyzer.mapper.RikishiMapper;
import com.sumo.sumo_analyzer.mapper.TorikumiResultMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RikishiService {

    private final RikishiMapper rikishiMapper;
    private final RikishiBashoRecordMapper bashoRecordMapper;
    private final TorikumiResultMapper torikumiResultMapper;

    public List<Rikishi> getAllRikishi() {
        List<Rikishi> rikishiList = rikishiMapper.findAll();
        for (Rikishi r : rikishiList) {
            if (r.getHeight() != null && r.getWeight() != null) {
                double heightCm = r.getHeight().doubleValue();
                double weightKg = r.getWeight().doubleValue();
                if (heightCm > 0) {
                    double heightM = heightCm / 100.0;
                    double rawBmi = weightKg / (heightM * heightM);
                    double roundedBmi = Math.round(rawBmi * 10.0) / 10.0;
                    r.setBmi(roundedBmi);
                }
            }
        }
        return rikishiList;
    }

    public Rikishi getRikishiById(Integer id) {
        return rikishiMapper.findById(id);
    }

    public List<RikishiBashoRecord> getRecordsByRikishiId(Integer id) {
        return bashoRecordMapper.findByRikishiId(id);
    }

    public List<Rikishi> getRikishiByCondition(String heya) {
        List<Rikishi> rikishiList = rikishiMapper.findByCondition(heya);
        for (Rikishi r : rikishiList) {
            if (r.getHeight() != null && r.getWeight() != null) {
                double heightM = r.getHeight().doubleValue() / 100.0;
                double weightKg = r.getWeight().doubleValue();
                if (heightM > 0) {
                    double rawBmi = weightKg / (heightM * heightM);
                    r.setBmi(Math.round(rawBmi * 10.0) / 10.0);
                }
            }
        }
        return rikishiList;
    }

    public List<String> getDistinctHeyaList() {
        return rikishiMapper.findDistinctHeya();
    }

    public List<Map<String, Object>> getWinningKimariteStats(Integer id) {
        return torikumiResultMapper.findWinningKimariteStats(id);
    }

    // ★新規追加：ライバル対戦成績を取得する
    public List<Map<String, Object>> getRivalStats(Integer id) {
        return torikumiResultMapper.findRivalStats(id);
    }

    // ★新規追加：Eloレーティング（戦闘力）の推移を取得する
    public List<Map<String, Object>> getEloHistory(Integer id) {
        return torikumiResultMapper.findEloHistory(id);
    }
}