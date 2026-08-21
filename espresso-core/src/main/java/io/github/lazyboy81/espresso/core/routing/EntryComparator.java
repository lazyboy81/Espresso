package io.github.lazyboy81.espresso.core.routing;

import java.util.Comparator;
import java.util.List;

final class EntryComparator implements Comparator<RouteEntry> {

    @Override
    public int compare(RouteEntry left, RouteEntry right) {
        List<RouteSegment> leftSegments = left.pattern().segments();
        int leftTotal = leftSegments.stream()
                .mapToInt(RouteSegment::score)
                .sum();

        List<RouteSegment> rightSegments = right.pattern().segments();
        int rightTotal = rightSegments.stream()
                .mapToInt(RouteSegment::score)
                .sum();

        // Higher total score comes first
        int totalComparison = Integer.compare(rightTotal, leftTotal);
        if (totalComparison != 0) {
            return totalComparison;
        }

        // Same total: compare each element in list order
        int sharedLength = Math.min(leftSegments.size(), rightSegments.size());

        for (int i = 0; i < sharedLength; i++) {
            int leftScore = leftSegments.get(i).score();
            int rightScore = rightSegments.get(i).score();

            // The first higher element score wins
            int elementComparison = Integer.compare(rightScore, leftScore);
            if (elementComparison != 0) {
                return elementComparison;
            }
        }

        // Only relevant if every compared score was equal
        return Integer.compare(leftSegments.size(), rightSegments.size());
    }

}
