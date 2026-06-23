package com.sumo.sumo_analyzer.service;

import com.sumo.sumo_analyzer.entity.Rikishi;
import com.sumo.sumo_analyzer.entity.TorikumiResult;
import com.sumo.sumo_analyzer.mapper.RikishiBashoRecordMapper;
import com.sumo.sumo_analyzer.mapper.RikishiMapper;
import com.sumo.sumo_analyzer.mapper.TorikumiResultMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SumoScraperService {

    private final RikishiMapper rikishiMapper;
    private final RikishiBashoRecordMapper bashoRecordMapper;
    private final TorikumiResultMapper torikumiResultMapper;

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private final String ACCEPT_LANG = "ja,ja-JP;q=0.9";

    public void executeHistoricalTorikumiScraping() {
        System.out.println("=== 究極の全自動スクレイピング（途中再開機能付き）を開始します ===");

        // ★ 自動再開（レジューム）機能：DBから最新の場所を取得
        String latestBasho = torikumiResultMapper.findLatestBasho();
        int startYear = 2013;
        int startMonth = 1;

        if (latestBasho != null && latestBasho.length() == 6) {
            startYear = Integer.parseInt(latestBasho.substring(0, 4));
            startMonth = Integer.parseInt(latestBasho.substring(4, 6));
            System.out.println("★DBから前回の中断箇所を検知しました！ [" + startYear + "年" + startMonth + "月場所] から再開します。");
        } else {
            System.out.println("★DBにデータがないため、一番最初の2013年1月場所から開始します。");
        }

        // ループ開始（startYear から開始）
        for (int year = startYear; year <= 2026; year++) {

            // ★ その年のスタート月を決める（再開する最初の年だけ startMonth から、翌年からは1月から）
            int initialMonth = (year == startYear) ? startMonth : 1;

            for (int month = initialMonth; month <= 11; month += 2) {
                String monthStr = String.format("%02d", month);
                String bashoStr = year + monthStr;

                try {
                    System.out.println("\n>>> [" + bashoStr + "場所] の裏IDを取得中...");

                    Document initialDoc = Jsoup.connect("https://www.sumo.or.jp/ResultRikishiDataDaicho/torikumi/")
                            .userAgent(USER_AGENT).header("Accept-Language", ACCEPT_LANG)
                            .data("year", String.valueOf(year))
                            .data("basho_month", monthStr)
                            .data("contents", "torikumi")
                            .post();

                    Matcher idMatcher = Pattern.compile("torikumi\\(\\d+,\\d+,(\\d+)\\)").matcher(initialDoc.html());
                    String bashoId = null;
                    if (idMatcher.find()) {
                        bashoId = idMatcher.group(1);
                    }

                    if (bashoId == null) {
                        continue;
                    }

                    // 階級のループ（1:幕内 〜 6:序ノ口）
                    for (int rank = 1; rank <= 6; rank++) {
                        // 日のループ（1〜15日目）
                        for (int day = 1; day <= 15; day++) {
                            scrapeSingleDayTorikumi(bashoStr, bashoId, day, rank);

                            // ★ サーバーに優しい安全な1.5秒待機
                            Thread.sleep(1500);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("場所ループ内でエラー: " + e.getMessage());
                }
            }
        }
        System.out.println("=== 13年分の全自動スクレイピングが完了しました！ ===");
    }

    private void scrapeSingleDayTorikumi(String bashoStr, String bashoId, int day, int rank) {
        String targetUrl = "https://www.sumo.or.jp/ResultRikishiDataDaicho/torikumi";

        try {
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent(USER_AGENT)
                    .header("Accept-Language", ACCEPT_LANG)
                    .data("basho_id", bashoId)
                    .data("kakuzuke", String.valueOf(rank))
                    .data("day", String.valueOf(day))
                    .post();

            Elements rows = doc.select("table.mdTable1 tr:has(td.player), table.mdTable1 tr:has(td.win)");
            if (rows.isEmpty()) return;

            int count = 0;
            for (Element row : rows) {
                try {
                    Elements nameSpans = row.select("td .name");
                    if (nameSpans.size() < 2) continue;

                    String eastShikona = nameSpans.get(0).text().replace(" ", "").replace(" ", "").trim();
                    String westShikona = nameSpans.get(1).text().replace(" ", "").replace(" ", "").trim();
                    if(eastShikona.isEmpty() || westShikona.isEmpty()) continue;

                    Element eastLinkEl = nameSpans.get(0).selectFirst("a");
                    String eastProfileUrl = (eastLinkEl != null) ? eastLinkEl.attr("href") : null;

                    Element westLinkEl = nameSpans.get(1).selectFirst("a");
                    String westProfileUrl = (westLinkEl != null) ? westLinkEl.attr("href") : null;

                    Integer eastId = getOrCreateRikishi(eastShikona, eastProfileUrl);
                    Integer westId = getOrCreateRikishi(westShikona, westProfileUrl);
                    if (eastId == null || westId == null) continue;

                    String kimarite = "";
                    Element kimariteEl = row.selectFirst("td.decide .technic");
                    if (kimariteEl != null) kimarite = kimariteEl.text().trim();

                    Integer winnerRikishiId = null;
                    Elements resultTds = row.select("td.result");
                    if (resultTds.size() >= 2) {
                        if (resultTds.get(0).hasClass("win") || resultTds.get(0).html().contains("ic01")) {
                            winnerRikishiId = eastId;
                        } else if (resultTds.get(1).hasClass("win") || resultTds.get(1).html().contains("ic01")) {
                            winnerRikishiId = westId;
                        }
                    }

                    TorikumiResult result = new TorikumiResult();
                    result.setBasho(bashoStr);
                    result.setDay(day);
                    result.setEastRikishiId(eastId);
                    result.setWestRikishiId(westId);
                    result.setWinnerRikishiId(winnerRikishiId);
                    result.setKimarite(kimarite);

                    if (torikumiResultMapper.countByBashoAndDayAndRikishi(bashoStr, day, eastId, westId) == 0) {
                        torikumiResultMapper.insert(result);
                        count++;
                    }
                } catch (Exception e) {}
            }
            if(count > 0) {
                System.out.println("  └ " + bashoStr + "場所 " + day + "日目 (段:" + rank + ") : " + count + "件保存");
            }
        } catch (Exception e) {}
    }

    private Integer getOrCreateRikishi(String shikona, String profileUrl) {
        Integer existingId = rikishiMapper.findIdByShikona(shikona);
        if (existingId != null) return existingId;

        System.out.println("    新力士を学習中... : " + shikona);
        Rikishi newRikishi = new Rikishi();
        newRikishi.setShikona(shikona);

        if (profileUrl != null && !profileUrl.isEmpty()) {
            try {
                String fullUrl = "https://www.sumo.or.jp" + profileUrl;
                Document doc = Jsoup.connect(fullUrl).userAgent(USER_AGENT).header("Accept-Language", ACCEPT_LANG).get();
                Elements rows = doc.select("table tr");
                for (Element row : rows) {
                    Element th = row.selectFirst("th");
                    Element td = row.selectFirst("td");
                    if (th != null && td != null) {
                        String header = th.text();
                        String data = td.text();
                        if (header.contains("所属部屋")) newRikishi.setHeya(data.trim());
                        else if (header.contains("身長")) {
                            String hStr = data.replace("cm", "").replace(" ", "").trim();
                            if (!hStr.isEmpty()) newRikishi.setHeight(new java.math.BigDecimal(hStr));
                        } else if (header.contains("体重")) {
                            String wStr = data.replace("kg", "").replace(" ", "").trim();
                            if (!wStr.isEmpty()) newRikishi.setWeight(new java.math.BigDecimal(wStr));
                        }
                    }
                }
            } catch (Exception e) {}
        }

        rikishiMapper.insert(newRikishi);
        return rikishiMapper.findIdByShikona(shikona);
    }
}