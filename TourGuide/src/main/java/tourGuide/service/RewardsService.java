package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

/**
 * This class is used by the TourGuideService class.
 * This class contains several methods to manage user rewards
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil GPS_UTIL;
    private final RewardCentral REWARD_CENTRAL;
    private Executor executor = Executors.newFixedThreadPool(100);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.GPS_UTIL = gpsUtil;
        this.REWARD_CENTRAL = rewardCentral;
    }

    /**
     * This method is used to set the proximity buffer
     *
     * @param proximityBuffer object
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * This method is used to set the proximity buffer to default value
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * This method calculates rewards for a user and contains ASYNCHRONOUS TECHNOLOGY
     * When this method is called, GPS_UTIL.getAttractions() method is running into a separated thread by supplyAsync method
     * which is waiting for the result. When the result of getAttractions() method id available,
     * thenAccept method use it for next operations
     *
     * @param user of type User
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        CompletableFuture.supplyAsync(() -> GPS_UTIL.getAttractions(), executor).thenAccept(attractions -> {
            for (VisitedLocation visitedLocation : userLocations) {
                for (Attraction attraction : attractions) {
                    if (user.getUserRewards().stream()
                            .filter(r -> r.ATTRACTION.attractionName.equals(attraction.attractionName)).count() == 0) {
                        if (nearAttraction(visitedLocation, attraction)) {
                            user.addUserReward(
                                    new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                        }
                    }
                }
            }
        });
    }

    /**
     * This method get the user rewards points
     *
     * @param attraction object, user object
     * @return int number
     */
    private int getRewardPoints(Attraction attraction, User user) {
        return REWARD_CENTRAL.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * This method is used to define if a user is in the attraction perimeter
     *
     * @param attraction object, location object
     * @return boolean
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    /**
     * This method is used to define if a user has visited an attraction
     *
     * @param attraction object, location object
     * @return boolean
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    /**
     * This method is used to calculate the distance between two locations
     *
     * @param loc1 of Location type, loc2 of Location type
     * @return double distance between this two locations
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }
}
