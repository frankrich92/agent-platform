package com.htam.agent.governance.eval;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EvalRunner {

    public EvalResult evaluate(EvalSample sample, String runId, String output) {
        if (sample == null) {
            throw new IllegalArgumentException("EvalSample 不能为空");
        }
        String text = output == null ? "" : output;
        List<String> signals = sample.expectedSignals();
        long matched = signals.stream().filter(text::contains).count();
        boolean passed = signals.isEmpty() || matched == signals.size();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("expectedSignals", signals.size());
        metrics.put("matchedSignals", matched);
        metrics.put("matchRate", signals.isEmpty() ? 1.0 : (double) matched / signals.size());
        return new EvalResult("eval-" + UUID.randomUUID(), sample.sampleId(), runId, passed, metrics, null);
    }
}
