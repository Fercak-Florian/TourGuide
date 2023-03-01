package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.utils.ProximityAttraction;
import tripPricer.Provider;

public class TestTourGuideService {
	
	@Before
	public void init() {
		Locale.setDefault(new Locale("en", "US", "WIN"));
	}
	
	@Test
	public void getUserLocation() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral() ,rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		
		/*ACT*/
		tourGuideService.trackUserLocation(user);
		while(user.getVisitedLocations().isEmpty()) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		VisitedLocation visitedLocation = user.getLastVisitedLocation();
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral() ,rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		/*ACT*/
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral() ,rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		/*ACT*/
		List<User> allUsers = tourGuideService.getAllUsers();
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil,new RewardCentral() ,rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		
		/*ACT*/
		tourGuideService.trackUserLocation(user);
		
		while(user.getVisitedLocations().isEmpty()) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		VisitedLocation visitedLocation = user.getLastVisitedLocation();
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	@Test
	public void getNearbyAttractions() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral(), rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user);
		
		while(user.getVisitedLocations().isEmpty()) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		VisitedLocation visitedLocation = user.getLastVisitedLocation();
		/*ACT*/
		List<ProximityAttraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertEquals(5, attractions.size());
	}
	
	@Test
	public void getTripDeals() {
		/*ARRANGE*/
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, new RewardCentral(), rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		
		/*ACT*/
		List<Provider> providers = tourGuideService.getTripDeals(user);
		tourGuideService.tracker.stopTracking();
		
		/*ASSERT*/
		assertEquals(5, providers.size());
	}
	
}