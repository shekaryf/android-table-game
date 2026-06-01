package ir.baran.bookPack.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import ir.baran.bookPack.api.model.LeaderboardEntry;
import ir.baran.bookPack.api.model.LeaderboardResult;

/**
 * API جدول امتیازات.
 */
public class LeaderboardApi extends BaseApiClient {

    // این مسیرها را روی سرور پیاده‌سازی کنید.
    private static final String URL_TOP10 = "https://baranapp.ir/ApiCarts/leaderboard/top10";
    private static final String URL_SUBMIT_AND_RANK = "https://baranapp.ir/ApiCarts/leaderboard/submit";

    public LeaderboardResult submitAndFetch(String name, String mobile, int level, int score, String userId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("mobile", mobile);
        body.put("level", level);
        body.put("score", score);
        body.put("id", userId == null ? "" : userId);

        String submitResponse = executePostJson(URL_SUBMIT_AND_RANK, body);
        LeaderboardResult result = parseResult(submitResponse);

        // اگر top10 در پاسخ submit نبود، از endpoint جداگانه بگیر.
        if (result.topEntries.isEmpty()) {
            String topResponse = executeGet(URL_TOP10);
            LeaderboardResult topResult = parseResult(topResponse);
            result.topEntries.addAll(topResult.topEntries);
        }

        // اگر rank شخصی در پاسخ نیامد، حداقل ردیف 11 را محلی بساز.
        if (result.selfEntry == null) {
            result.selfEntry = new LeaderboardEntry(userId == null ? "" : userId, name, level, score, 11);
        }
        return result;
    }

    public void submitOnly(String name, String mobile, int level, int score, String userId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("mobile", mobile);
        body.put("level", level);
        body.put("score", score);
        body.put("id", userId == null ? "" : userId);
        executePostJson(URL_SUBMIT_AND_RANK, body);
    }

    private LeaderboardResult parseResult(String raw) throws Exception {
        LeaderboardResult result = new LeaderboardResult();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }

        String text = raw.trim();
        if (text.startsWith("[")) {
            JSONArray arr = new JSONArray(text);
            parseTopArray(result, arr);
            return result;
        }

        JSONObject root = new JSONObject(text);
        String rootId = extractId(root);
        JSONArray top = root.optJSONArray("top");
        if (top == null) {
            top = root.optJSONArray("top10");
        }
        if (top != null) {
            parseTopArray(result, top);
        }

        JSONObject self = root.optJSONObject("self");
        if (self == null) {
            JSONArray selfArr = root.optJSONArray("self");
            if (selfArr != null && selfArr.length() > 0) {
                self = selfArr.optJSONObject(0);
            }
        }
        if (self == null) {
            self = root.optJSONObject("me");
        }
        if (self == null) {
            JSONArray meArr = root.optJSONArray("me");
            if (meArr != null && meArr.length() > 0) {
                self = meArr.optJSONObject(0);
            }
        }
        if (self != null) {
            result.selfEntry = parseEntry(self, 11);
            if (result.selfEntry != null && isBlank(result.selfEntry.id) && !isBlank(rootId)) {
                result.selfEntry = new LeaderboardEntry(
                        rootId,
                        result.selfEntry.name,
                        result.selfEntry.level,
                        result.selfEntry.score,
                        result.selfEntry.rank
                );
            }
        } else if (!isBlank(rootId)) {
            // بعضی پاسخ‌ها id را در ریشه برمی‌گردانند و self آبجکت ندارند.
            result.selfEntry = new LeaderboardEntry(rootId, "بازیکن", 1, 0, 11);
        }
        return result;
    }

    private void parseTopArray(LeaderboardResult result, JSONArray arr) {
        int rank = 1;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject item = arr.optJSONObject(i);
            if (item == null) {
                continue;
            }
            result.topEntries.add(parseEntry(item, rank));
            rank++;
            if (result.topEntries.size() >= 10) {
                break;
            }
        }
    }

    private LeaderboardEntry parseEntry(JSONObject obj, int fallbackRank) {
        String id = extractId(obj);
        String name = obj.optString("name", "بازیکن");
        int level = obj.optInt("level", 1);
        int score = obj.optInt("score", 0);
        int rank = obj.optInt("rank", fallbackRank);
        return new LeaderboardEntry(id, name, level, score, rank);
    }

    private String extractId(JSONObject obj) {
        if (obj == null) {
            return "";
        }
        // ساپورت چند کلید متداول
        String id = normalizeId(obj.optString("id", ""));
        if (!isBlank(id)) return id;
        id = normalizeId(obj.optString("Id", ""));
        if (!isBlank(id)) return id;
        id = normalizeId(obj.optString("self_id", ""));
        if (!isBlank(id)) return id;
        id = normalizeId(obj.optString("user_id", ""));
        if (!isBlank(id)) return id;

        // اگر عددی بود، به رشته تبدیل شود
        long idLong = obj.optLong("id", Long.MIN_VALUE);
        if (idLong != Long.MIN_VALUE) {
            return String.valueOf(idLong);
        }
        return "";
    }

    private String normalizeId(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if ("null".equalsIgnoreCase(v)) {
            return "";
        }
        return v;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
