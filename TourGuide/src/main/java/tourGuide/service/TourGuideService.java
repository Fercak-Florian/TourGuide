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
import tourGuide.utils.ProximityAttraction;
import tourGuide.utils.UserMostRecentLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * This class is used by the controller class.
 * This class contains several methods to get user information such as his GPS position,
 * his trip deals providers and his 5 closest attractions
 */
@Service
public class TourGuideService {

	@Autowired
	UserService userService;

	private final GpsUtil GPS_UTIL;
	private final RewardCentral REWARD_CENTRAL;
	private final RewardsService REWARD_SERVICE;
	private final TripPricer TRIP_PRICER = new TripPricer();
	public final Tracker tracker;
	private static final String TRIP_PRICER_API_KEY = "test-server-api-key";

	private Executor executor = Executors.newFixedThreadPool(100);

	public TourGuideService(GpsUtil gpsUtil, RewardCentral rewardCentral, RewardsService rewardsService) {
		this.GPS_UTIL = gpsUtil;
		this.REWARD_CENTRAL = rewardCentral;
		this.REWARD_SERVICE = rewardsService;

		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * This method gets the user GPS position
	 * @Param a User object
	 * @return a VisitedLocation containing the user id, the user Location and a date
	 */
	public VisitedLocation getUserLocation(User user) {
		if (user.getVisitedLocations().isEmpty()) {
			trackUserLocation(user);
			return null;
		} else {
			return user.getLastVisitedLocation();
		}
	}

	/**
	 * This method gets all the most recent user GPS position
	 *
	 * @return a List of UserMostRecentLocation, each element of the list contains a user id
	 * and the last user GPS position
	 */
	public List<UserMostRecentLocation> getAllUsersMostRecentLocation() {
		List<User> users = userService.getAllUsers();
		List<UserMostRecentLocation> usersMostRecentLocations = new ArrayList<>();
		for (User user : users) {
			usersMostRecentLocations
					.add(new UserMostRecentLocation(user.getUserId(), user.getLastVisitedLocation().location));
		}
		return usersMostRecentLocations;
	}

	/**
	 * This method gets the user trip deals
	 *
	 * @return a List of Provider, each element of the list contains a travel provider name,
	 * the travel price and a trip id
	 */
	public List<Provider> getTripDeals(User user) {
		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = TRIP_PRICER.getPrice(TRIP_PRICER_API_KEY, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * This method get the user most recent GPS position and calculate the user rewards
	 *
	 * @param user of type User
	 */
	public void trackUserLocation(User user) {
		CompletableFuture.supplyAsync(() -> GPS_UTIL.getUserLocation(user.getUserId()), executor).thenAccept(v -> {
			user.addToVisitedLocations(v);
			REWARD_SERVICE.calculateRewards(user);
		});
	}

	/**
	 * This method gets the 5 closest attractions from the user GPS position
	 *
	 * @param visitedLocation
	 * @return List of ProximityAttraction, each element of the list contains the attraction GPS position,
	 * the GPS user position, the distance in miles between the user and the attraction,
	 * the user rewards points and the attraction name.
	 */
	public List<ProximityAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
		Map<Attraction, Double> nearestAttractions = new HashMap<>();

		for (Attraction attraction : GPS_UTIL.getAttractions()) {
			nearestAttractions.put(attraction, REWARD_SERVICE.getDistance(visitedLocation.location, attraction));
		}
		return (nearestAttractions.entrySet().stream().sorted(Entry.comparingByValue()).limit(5)
				.map(t -> new ProximityAttraction(t.getKey().attractionName, t.getKey().latitude, t.getKey().longitude,
						visitedLocation.location, t.getValue(),
						REWARD_CENTRAL.getAttractionRewardPoints(t.getKey().attractionId, visitedLocation.userId)))
				.collect(Collectors.toList()));
	}

	/**
	 * This method is used to stop the tracker running in a separated thread
	 */
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}
}
