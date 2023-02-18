package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	public CompletableFuture<Void> calculateRewards1(User user) {
		CompletableFuture<Void> future = null;
		CompletableFuture<Void> waitingForAllFutures = null;
		List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
				for (Attraction attraction : attractions) {
					if (user.getUserRewards().stream()
							.filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
						if (nearAttraction(visitedLocation, attraction)) {
							 future = CompletableFuture.supplyAsync(() -> getRewardPoints(attraction, user))
							.thenAccept(rewards -> {
								user.addUserReward(new UserReward(visitedLocation, attraction, rewards));
							});
					}
				}
			};
			completableFutures.add(future);
		}
		waitingForAllFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

		return waitingForAllFutures;
	}

	public CompletableFuture<Void> calculateRewards(User user) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();
		CompletableFuture<Void> waitingForAllFutures = null;

		for (VisitedLocation visitedLocation : userLocations) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				for (Attraction attraction : attractions) {
					if (user.getUserRewards().stream()
							.filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
						if (nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(
									new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			});
			futures.add(future);
		}
		waitingForAllFutures = CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[futures.size()]));

		return waitingForAllFutures;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

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
