package ir.baran.bookPack.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ir.baran.bookPack.BazaarPay;

/**
 * سرویس API مربوط به بسته‌های امتیاز.
 */
public class ScorePlansApi extends BaseApiClient {

    private static final String SCORE_PLANS_API_URL = "https://baranapp.ir/ApiCarts/scores";

    public List<BazaarPay.ScorePackPlan> fetchPlans() throws Exception {
        String response = executeGet(SCORE_PLANS_API_URL);
        JSONArray array = new JSONArray(response);
        List<BazaarPay.ScorePackPlan> plans = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            String sku = obj.optString("name", "").trim();
            int price = obj.optInt("price", 0);
            int score = obj.optInt("score", 0);
            if (sku.isEmpty() || price <= 0 || score <= 0) {
                continue;
            }
            plans.add(new BazaarPay.ScorePackPlan(sku, score, price));
        }
        return plans;
    }
}

