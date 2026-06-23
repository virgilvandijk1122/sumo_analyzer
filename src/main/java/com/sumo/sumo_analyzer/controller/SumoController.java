package com.sumo.sumo_analyzer.controller;

import com.sumo.sumo_analyzer.entity.Rikishi;
import com.sumo.sumo_analyzer.service.EloCalculationService;
import com.sumo.sumo_analyzer.service.RikishiService;
import com.sumo.sumo_analyzer.service.SumoScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SumoController {

    private final RikishiService rikishiService;
    private final SumoScraperService scraperService;
    private final EloCalculationService eloCalculationService;

    @GetMapping("/list")
    public String showList(@RequestParam(value = "heya", required = false) String heya, Model model) {
        List<Rikishi> rikishiList;

        if (heya != null && !heya.isEmpty()) {
            rikishiList = rikishiService.getRikishiByCondition(heya);
        } else {
            rikishiList = rikishiService.getAllRikishi();
        }

        List<String> heyaList = rikishiService.getDistinctHeyaList();

        model.addAttribute("rikishiList", rikishiList);
        model.addAttribute("heyaList", heyaList);
        model.addAttribute("selectedHeya", heya);

        return "list";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable Integer id, Model model) {
        com.sumo.sumo_analyzer.entity.Rikishi rikishi = rikishiService.getRikishiById(id);
        List<com.sumo.sumo_analyzer.entity.RikishiBashoRecord> records = rikishiService.getRecordsByRikishiId(id);

        int totalWins = 0;
        int totalLosses = 0;
        int totalAbsences = 0;

        for (com.sumo.sumo_analyzer.entity.RikishiBashoRecord record : records) {
            totalWins += record.getWins();
            totalLosses += record.getLosses();
            totalAbsences += record.getAbsences();
        }

        List<Map<String, Object>> kimariteStats = rikishiService.getWinningKimariteStats(id);

        // ★新規追加：ライバルの対戦成績を取得
        List<Map<String, Object>> rivalStats = rikishiService.getRivalStats(id);
        // ★新規追加：Eloレーティング推移を取得
        List<Map<String, Object>> eloHistory = rikishiService.getEloHistory(id);

        model.addAttribute("rikishi", rikishi);
        model.addAttribute("records", records);
        model.addAttribute("totalWins", totalWins);
        model.addAttribute("totalLosses", totalLosses);
        model.addAttribute("totalAbsences", totalAbsences);
        model.addAttribute("kimariteStats", kimariteStats);
        model.addAttribute("rivalStats", rivalStats); // ★追加
        model.addAttribute("eloHistory", eloHistory);

        return "detail";
    }

    @GetMapping("/admin/scrape-torikumi")
    public String triggerTorikumiScraping() {
        scraperService.executeHistoricalTorikumiScraping();
        return "redirect:/list";
    }

    @GetMapping("/admin/calc-elo")
    public String triggerEloCalculation() {
        eloCalculationService.calculateAllElo();
        return "redirect:/list";
    }
}