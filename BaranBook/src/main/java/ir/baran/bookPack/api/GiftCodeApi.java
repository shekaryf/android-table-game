package ir.baran.bookPack.api;

import org.json.JSONObject;

public class GiftCodeApi extends BaseApiClient {

    private static final String URL_GIFT = "https://baranapp.ir/ApiCarts/leaderboard/gift";

    public static class GiftResult {
        public final boolean success;
        public final int score;

        public GiftResult(boolean success, int score) {
            this.success = success;
            this.score = score;
        }
    }

    public GiftResult applyGiftCode(String code) throws Exception {
        JSONObject body = new JSONObject();
        body.put("code", code == null ? "" : code.trim());
        String response = executePostJson(URL_GIFT, body);
        JSONObject obj = new JSONObject(response);
        boolean success = obj.optBoolean("success", false);
        int score = obj.optInt("score", 0);
        return new GiftResult(success, score);
    }
}

