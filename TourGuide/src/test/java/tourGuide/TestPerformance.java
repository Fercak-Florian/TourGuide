package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Before
	public void init() {
		Locale.setDefault(new Locale("en", "US", "WIN"));
	}

	@Test
	public void highVolumeTrackLocation() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral(), rewardsService);

		List<User> allUsers = userService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Map<User, Integer> usersBeforeTracking = new HashMap<>();
		for (User user : allUsers) {
			usersBeforeTracking.put(user, user.getVisitedLocations().size());
		}
		
		/*ACT*/
		for (User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}
		/*Waiting for user's update*/
		for (User user : allUsers) {
			while (user.getVisitedLocations().size() <= usersBeforeTracking.get(user)) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		/*ASSERT*/
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral() ,rewardsService);
		
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = userService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		/*ACT*/
	    allUsers.forEach(u -> rewardsService.calculateRewards(u));

		/*Waiting for user's update*/
		for(User user : allUsers) {
			while(user.getUserRewards().isEmpty()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		/*ASSERT*/
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
