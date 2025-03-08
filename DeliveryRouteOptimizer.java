package Arrays;


import java.util.*;

public class AdvancedRoutingSystem {

    // Location class to represent each location
    static class Location {
        int id;
        String name;

        public Location(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Path class to represent a path between two locations
    static class Path {
        int from;
        int to;
        double distance; // Base distance
        double trafficFactor; // Traffic impact (1.0 = no delay)
        double weatherImpact; // Weather impact (1.0 = clear, >1 = adverse)
        double elevationImpact; // Elevation (1.0 = flat)
        double hazardRisk; // Hazard risk (1.0 = no risk)

        public Path(int from, int to, double distance, double trafficFactor, double weatherImpact, double elevationImpact, double hazardRisk) {
            this.from = from;
            this.to = to;
            this.distance = distance;
            this.trafficFactor = trafficFactor;
            this.weatherImpact = weatherImpact;
            this.elevationImpact = elevationImpact;
            this.hazardRisk = hazardRisk;
        }

        // Adjusted cost calculation considering multiple factors
        public double getAdjustedCost() {
            return distance * (1 + trafficFactor + weatherImpact + elevationImpact + hazardRisk);
        }
    }

    // Dijkstra-based routing with multi-factor cost
    public static Map<Integer, Double> findOptimalRoute(Map<Integer, Location> locations, List<Path> paths, int startId, boolean considerTraffic, boolean considerWeather, boolean considerHazards, boolean considerElevation) {
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Location> locationById = new HashMap<>(locations); // Map location by id
        PriorityQueue<Location> pq = new PriorityQueue<>(Comparator.comparingDouble(loc -> distances.get(loc.id))); // Compare locations by distance
        Map<Integer, Boolean> visited = new HashMap<>();

        // Initialize distances for all locations
        for (Location loc : locations.values()) {
            distances.put(loc.id, Double.MAX_VALUE);  // Initialize all distances to infinity
        }
        distances.put(startId, 0.0);  // Set the start location's distance to 0
        pq.add(locationById.get(startId));  // Add the start location to the priority queue

        while (!pq.isEmpty()) {
            Location currentLocation = pq.poll();
            int currentId = currentLocation.id;

            // Skip already visited locations
            if (visited.containsKey(currentId)) {
                continue;
            }
            visited.put(currentId, true);

            // Process adjacent locations
            for (Path path : paths) {
                if (path.from == currentId || path.to == currentId) {
                    int nextId = (path.from == currentId) ? path.to : path.from;

                    double adjustedCost = path.getAdjustedCost();

                    // Adjust cost for dynamic factors
                    if (considerTraffic) {
                        adjustedCost += predictTraffic(path);
                    }
                    if (considerWeather) {
                        adjustedCost += path.weatherImpact;
                    }
                    if (considerHazards) {
                        adjustedCost += path.hazardRisk;
                    }
                    if (considerElevation) {
                        adjustedCost += path.elevationImpact;
                    }

                    // Update the distance if the new path is shorter
                    if (distances.get(currentId) + adjustedCost < distances.get(nextId)) {
                        distances.put(nextId, distances.get(currentId) + adjustedCost);
                        pq.add(locationById.get(nextId));  // Add updated location to the queue
                    }
                }
            }
        }

        return distances;
    }

    // Simulate traffic prediction based on past data (simplified)
    private static double predictTraffic(Path path) {
        return path.trafficFactor > 1.5 ? 0.2 * path.distance : 0.0;  // More traffic adds extra time
    }

    // Simulate weather impact calculation
    private static double getWeatherImpact(String weatherCondition) {
        switch (weatherCondition) {
            case "rain": return 0.3;
            case "snow": return 0.6;
            default: return 0.0; // No impact for clear weather
        }
    }

    // Hazard detection function
    private static double getHazardRisk(String hazardCondition) {
        switch (hazardCondition) {
            case "high_risk": return 0.4;
            case "moderate_risk": return 0.2;
            default: return 0.0; // No risk
        }
    }

    // Elevation adjustment function (simulate road elevation impact)
    private static double getElevationImpact(String elevationType) {
        switch (elevationType) {
            case "hilly": return 0.3;
            case "mountainous": return 0.5;
            default: return 0.0; // Flat terrain has no impact
        }
    }

    // Main method to test routing
    public static void main(String[] args) {
        // Locations
        Map<Integer, Location> locations = new HashMap<>();
        locations.put(1, new Location(1, "A"));
        locations.put(2, new Location(2, "B"));
        locations.put(3, new Location(3, "C"));
        locations.put(4, new Location(4, "D"));

        // Paths (with traffic, weather, elevation, hazard risk)
        List<Path> paths = new ArrayList<>();
        paths.add(new Path(1, 2, 5.0, 0.2, getWeatherImpact("rain"), getElevationImpact("flat"), getHazardRisk("no_risk"))); // A to B
        paths.add(new Path(2, 3, 10.0, 0.3, getWeatherImpact("snow"), getElevationImpact("hilly"), getHazardRisk("high_risk"))); // B to C
        paths.add(new Path(3, 4, 7.0, 0.1, getWeatherImpact("clear"), getElevationImpact("mountainous"), getHazardRisk("moderate_risk"))); // C to D
        paths.add(new Path(1, 4, 15.0, 0.4, getWeatherImpact("rain"), getElevationImpact("flat"), getHazardRisk("no_risk"))); // A to D

        // Testing with all factors considered
        Map<Integer, Double> route = findOptimalRoute(locations, paths, 1, true, true, true, true);

        // Output the optimized route distances considering all factors
        System.out.println("Optimized route distances (with all factors considered):");
        route.forEach((id, distance) -> {
            System.out.println("Location " + locations.get(id).name + ": " + distance + " units");
        });

        // Testing with no hazard consideration
        Map<Integer, Double> routeWithoutHazards = findOptimalRoute(locations, paths, 1, true, true, false, true);
        System.out.println("\nOptimized route distances (without hazards considered):");
        routeWithoutHazards.forEach((id, distance) -> {
            System.out.println("Location " + locations.get(id).name + ": " + distance + " units");
        });

        // Testing with no traffic or weather consideration
        Map<Integer, Double> routeWithoutTrafficWeather = findOptimalRoute(locations, paths, 1, false, false, true, true);
        System.out.println("\nOptimized route distances (without traffic or weather considered):");
        routeWithoutTrafficWeather.forEach((id, distance) -> {
            System.out.println("Location " + locations.get(id).name + ": " + distance + " units");
        });
    }
}
