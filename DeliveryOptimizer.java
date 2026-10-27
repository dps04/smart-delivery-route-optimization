package com.logismart;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Core allocation, route-planning and KPI logic for the educational prototype. */
public class DeliveryOptimizer {
    public record Order(String id, String zone, int load, String priority, String vehicleType) {}
    public record Vehicle(String id, String type, int capacity, boolean available) {}
    public record VehiclePlan(String vehicleId, List<String> zones, int load, int capacity, List<String> baselineRoute, List<String> optimizedRoute, int baselineKm, int optimizedKm) {}
    public record Result(int orders, int allocated, int baselineKm, int optimizedKm, double reductionPercent, List<VehiclePlan> plans, List<String> warnings) {}

    private final Map<String, Map<String, Integer>> distances = new LinkedHashMap<>();

    public Result run(Path dataDir) throws IOException {
        List<Order> orders = readOrders(dataDir.resolve("Delivery_Orders_Sample.csv"));
        List<Vehicle> vehicles = readVehicles(dataDir.resolve("Vehicle_Master_Sample.csv"));
        readDistances(dataDir.resolve("Zone_Distance_Matrix_Sample.csv"));
        List<String> warnings = new ArrayList<>();
        validate(orders, vehicles, warnings);

        Map<String, Integer> vehicleLoads = new LinkedHashMap<>();
        Map<String, List<Order>> assigned = new LinkedHashMap<>();
        for (Vehicle v : vehicles) { vehicleLoads.put(v.id(), 0); assigned.put(v.id(), new ArrayList<>()); }
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort(Comparator.comparingInt((Order o) -> priorityRank(o.priority())).thenComparing(Order::id));
        List<Order> unassigned = new ArrayList<>();
        for (Order o : sorted) {
            Vehicle chosen = vehicles.stream().filter(v -> v.available() && v.type().equalsIgnoreCase(o.vehicleType()))
                .filter(v -> vehicleLoads.get(v.id()) + o.load() <= v.capacity())
                .findFirst().orElse(null);
            if (chosen == null) { unassigned.add(o); warnings.add("Order " + o.id() + " could not be assigned (vehicle availability/type/capacity). "); }
            else { assigned.get(chosen.id()).add(o); vehicleLoads.put(chosen.id(), vehicleLoads.get(chosen.id()) + o.load()); }
        }
        List<VehiclePlan> plans = new ArrayList<>(); int baseTotal = 0, optTotal = 0;
        for (Vehicle v : vehicles) {
            List<Order> group = assigned.get(v.id());
            List<String> baseline = group.stream().map(Order::zone).toList();
            List<String> optimized = nearestNeighbour(baseline);
            int baseKm = routeDistance(baseline), optKm = routeDistance(optimized);
            plans.add(new VehiclePlan(v.id(), baseline, vehicleLoads.get(v.id()), v.capacity(), baseline, optimized, baseKm, optKm));
            baseTotal += baseKm; optTotal += optKm;
        }
        double reduction = baseTotal == 0 ? 0.0 : (baseTotal - optTotal) * 100.0 / baseTotal;
        return new Result(orders.size(), orders.size() - unassigned.size(), baseTotal, optTotal, reduction, plans, warnings);
    }

    private static int priorityRank(String p) { return switch (p.toLowerCase(Locale.ROOT)) { case "high" -> 0; case "medium" -> 1; default -> 2; }; }
    private static List<Order> readOrders(Path path) throws IOException {
        List<Order> out = new ArrayList<>(); List<String> lines = Files.readAllLines(path);
        for (int i=1; i<lines.size(); i++) { if (lines.get(i).isBlank()) continue; String[] c=lines.get(i).split(",",-1); if(c.length<5) throw new IOException("Invalid order row: "+lines.get(i)); out.add(new Order(c[0].trim(),c[1].trim(),Integer.parseInt(c[2].trim()),c[3].trim(),c[4].trim())); }
        return out;
    }
    private static List<Vehicle> readVehicles(Path path) throws IOException {
        List<Vehicle> out = new ArrayList<>(); List<String> lines = Files.readAllLines(path);
        for (int i=1; i<lines.size(); i++) { if(lines.get(i).isBlank()) continue; String[] c=lines.get(i).split(",",-1); if(c.length<4) throw new IOException("Invalid vehicle row: "+lines.get(i)); out.add(new Vehicle(c[0].trim(),c[1].trim(),Integer.parseInt(c[2].trim()),c[3].trim().equalsIgnoreCase("yes"))); }
        return out;
    }
    private void readDistances(Path path) throws IOException {
        distances.clear(); List<String> lines=Files.readAllLines(path); if(lines.isEmpty()) throw new IOException("Distance matrix is empty");
        String[] header=lines.get(0).split(",",-1); List<String> zones=new ArrayList<>(); for(int i=1;i<header.length;i++) zones.add(header[i].trim().replace(" (Depot)",""));
        for(int r=1;r<lines.size();r++){ if(lines.get(r).isBlank()) continue; String[] c=lines.get(r).split(",",-1); String from=c[0].trim().replace(" (Depot)",""); Map<String,Integer> row=new HashMap<>(); for(int j=1;j<c.length && j<=zones.size();j++) row.put(zones.get(j-1),Integer.parseInt(c[j].trim())); distances.put(from,row); }
        if(!distances.containsKey("A")) throw new IOException("Distance matrix must include depot zone A (A (Depot) in the sample file).");
    }
    private void validate(List<Order> orders, List<Vehicle> vehicles, List<String> warnings) {
        Set<String> ids=new HashSet<>(); for(Order o:orders){ if(!ids.add(o.id())) warnings.add("Duplicate order ID: "+o.id()); if(o.load()<=0) warnings.add("Non-positive load on "+o.id()); if(!distances.containsKey(o.zone())) warnings.add("Unknown destination zone on "+o.id()+": "+o.zone()); }
        Set<String> vids=new HashSet<>(); for(Vehicle v:vehicles){ if(!vids.add(v.id())) warnings.add("Duplicate vehicle ID: "+v.id()); if(v.capacity()<=0) warnings.add("Non-positive capacity on "+v.id()); }
    }
    private List<String> nearestNeighbour(List<String> zones) {
        List<String> remaining=new ArrayList<>(zones), route=new ArrayList<>(); String current="A";
        while(!remaining.isEmpty()) { String next=null; int best=Integer.MAX_VALUE; for(String z:remaining){ int d=distance(current,z); if(d<best){best=d;next=z;} } route.add(next); remaining.remove(next); current=next; }
        return route;
    }
    private int routeDistance(List<String> zones) { int total=0; String current="A"; for(String z:zones){total+=distance(current,z);current=z;} if(!zones.isEmpty()) total+=distance(current,"A"); return total; }
    private int distance(String from,String to) { Integer d=distances.getOrDefault(from,Map.of()).get(to); if(d==null) throw new IllegalArgumentException("Missing distance from "+from+" to "+to); return d; }
}
