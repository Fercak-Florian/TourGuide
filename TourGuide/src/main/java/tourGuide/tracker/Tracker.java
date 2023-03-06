package tourGuide.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long TRACKING_POLLING_INTERVAL = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private final TourGuideService TOUR_GUIDE_SERVICE;
	private boolean stop = false;

	@Autowired
	UserService userService;

	public Tracker(TourGuideService tourGuideService) {
		this.TOUR_GUIDE_SERVICE = tourGuideService;
		
		EXECUTOR_SERVICE.submit(this);
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		EXECUTOR_SERVICE.shutdownNow();
	}
	
	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			
			List<User> users = userService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");

			stopWatch.start();

			Map<User, Integer> usersBeforeTracking = new HashMap<>();
			for(User user : users){
				usersBeforeTracking.put(user, user.getVisitedLocations().size());
			}

			users.forEach(u -> TOUR_GUIDE_SERVICE.trackUserLocation(u));

			for(User user : users){
				while(user.getVisitedLocations().size() <= usersBeforeTracking.get(user)){
					/*waiting*/
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

			stopWatch.stop();

			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TRACKING_POLLING_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
