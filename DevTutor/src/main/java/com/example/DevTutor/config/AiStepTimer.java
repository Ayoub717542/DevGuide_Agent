package com.example.DevTutor.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class AiStepTimer implements ObservationHandler<Observation.Context> {

    private static final String KEY = AiStepTimer.class.getName();

    private static final class Step {
        private final String name;
        private volatile String label;
        private final long start = System.nanoTime();
        private volatile long end;
        private final Queue<Step> children = new ConcurrentLinkedQueue<>();

        private Step(String name) {
            this.name = name;
            this.label = name;
        }

        private long duration() {
            return end - start;
        }
    }

    private record Totals(long self, long overlap, long outside) {}

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    @Override
    public void onStart(Observation.Context context) {
        try {
            if (isChatRequest(context)) {
                context.put(KEY, new Step("/api/chat"));
            } else if (isAiStep(context)) {
                context.put(KEY, new Step(context.getName()));
            }
        } catch (RuntimeException e) {
            log.debug("AiStepTimer start failed", e);
        }
    }

    @Override
    public void onStop(Observation.Context context) {
        try {
            Step step = context.get(KEY);
            if (step == null) {
                return;
            }
            step.end = System.nanoTime();
            String contextual = context.getContextualName();
            if (contextual != null && !contextual.isBlank()) {
                step.label = step.name + " (" + contextual + ")";
            }
            context.remove(KEY);
            if (isChatRequest(context)) {
                log.info(report(step));
                return;
            }
            Step parent = nearestTrackedAncestor(context);
            if (parent != null) {
                parent.children.add(step);
            }
        } catch (RuntimeException e) {
            log.debug("AiStepTimer stop failed", e);
        }
    }

    private static boolean isChatRequest(Observation.Context context) {
        return context instanceof ServerRequestObservationContext request
                && request.getCarrier() != null
                && "/api/chat".equals(request.getCarrier().getRequestURI());
    }

    private static boolean isAiStep(Observation.Context context) {
        String name = context.getName();
        return name != null
                && (name.startsWith("spring.ai.") || name.startsWith("gen_ai.") || name.startsWith("db.vector"));
    }

    private static Step nearestTrackedAncestor(Observation.Context context) {
        ObservationView parent = context.getParentObservation();
        while (parent != null) {
            Step step = parent.getContextView().get(KEY);
            if (step != null) {
                return step;
            }
            parent = parent.getContextView().getParentObservation();
        }
        return null;
    }

    private static String report(Step root) {
        StringBuilder out = new StringBuilder("AI timing tree (total = includes children, self = own time only)\n");
        Totals totals = appendTree(root, 0, out);
        long total = root.duration();
        out.append(String.format(Locale.ROOT,
                "check: self sum = %s, total = %s, overlap = %s, outside parent = %s, consistent = %b",
                ms(totals.self()), ms(total), ms(totals.overlap()), ms(totals.outside()),
                totals.self() == total + totals.overlap() + totals.outside()));
        return out.toString();
    }

    private static Totals appendTree(Step step, int depth, StringBuilder out) {
        List<Step> children = new ArrayList<>(step.children);
        children.sort(Comparator.comparingLong(child -> child.start));

        long union = 0;
        long clippedSum = 0;
        long outside = 0;
        long coveredUntil = step.start;
        for (Step child : children) {
            long from = Math.max(child.start, step.start);
            long to = Math.min(child.end, step.end);
            long clipped = Math.max(0, to - from);
            clippedSum += clipped;
            outside += child.duration() - clipped;
            if (to > coveredUntil) {
                union += to - Math.max(from, coveredUntil);
                coveredUntil = to;
            }
        }
        long self = step.duration() - union;

        out.append("  ".repeat(depth + 1)).append(step.label)
                .append(" | total ").append(ms(step.duration()))
                .append(" | self ").append(ms(self))
                .append(depth == 0 ? "  (time outside AI steps: DB, MCP tool listing, saving)" : "")
                .append("\n");

        long selfSum = self;
        long overlapSum = clippedSum - union;
        long outsideSum = outside;
        for (Step child : children) {
            Totals totals = appendTree(child, depth + 1, out);
            selfSum += totals.self();
            overlapSum += totals.overlap();
            outsideSum += totals.outside();
        }
        return new Totals(selfSum, overlapSum, outsideSum);
    }

    private static String ms(long nanos) {
        return String.format(Locale.ROOT, "%.1f ms", nanos / 1_000_000.0);
    }
}
