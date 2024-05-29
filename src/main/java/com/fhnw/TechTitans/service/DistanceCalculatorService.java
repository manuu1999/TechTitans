package com.fhnw.TechTitans.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceCalculatorService {

    // code from following source:
    // https://www.baeldung.com/java-find-distance-between-points
    // adapted to the project

    /**
     * Calculate the distance between two points on the Earth.
     * Haversine Formula
     *
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return Distance in kilometers.
     */
    public static double calculateDistance (double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius of the earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;


    }
}

