package tourGuide.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tourGuide.utils.ProximityAttraction;
import tourGuide.utils.UserMostRecentLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {

	@Autowired
	UserService userService;

	private final GpsUtil gpsUtil;
	private final RewardCentral rewardCentral;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	private static final String tripPricerApiKey = "test-server-api-key";

	Executor executor = Executors.newFixedThreadPool(100);

	public TourGuideService(GpsUtil gpsUtil, RewardCentral rewardCentral, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardCentral = rewardCentral;
		this.rewardsService = rewardsService;


		tracker = new Tracker(this);
		addShutDownHook();
	}

	public VisitedLocation getUserLocation(User user) {
		if (user.getVisitedLocations().isEmpty()) {
			trackUserLocation(user);
			return null;
		} else {
			return user.getLastVisitedLocation();
		}
	}

	public List<UserMostRecentLocation> getAllUsersMostRecentLocation() {
		List<User> users = userService.getAllUsers();
		List<UserMostRecentLocation> usersMostRecentLocations = new ArrayList<>();
		for (User user : users) {
			usersMostRecentLocations
					.add(new UserMostRecentLocation(user.getUserId(), user.getLastVisitedLocation().location));
		}
		return usersMostRecentLocations;
	}

	public List<Provider> getTripDeals(User user) {
		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public void trackUserLocation(User user) {
		CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executor).thenAccept(v -> {
			user.addToVisitedLocations(v);
			rewardsService.calculateRewards(user);
		});
	}

	public List<ProximityAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
		Map<Attraction, Double> nearestAttractions = new HashMap<>();

		for (Attraction attraction : gpsUtil.getAttractions()) {
			nearestAttractions.put(attraction, rewardsService.getDistance(visitedLocation.location, attraction));
		}
		return (nearestAttractions.entrySet().stream().sorted(Entry.comparingByValue()).limit(5)
				.map(t -> new ProximityAttraction(t.getKey().attractionName, t.getKey().latitude, t.getKey().longitude,
						visitedLocation.location, t.getValue(),
						rewardCentral.getAttractionRewardPoints(t.getKey().attractionId, visitedLocation.userId)))
				.collect(Collectors.toList()));
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}
}
