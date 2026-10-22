package my.packagename.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Our {@code @Scheduled} jobs get their own scheduler, named explicitly: the outbox SDK registers a
 * private {@code TaskScheduler} too, and type-based lookup must never hand it our jobs.
 *
 * <p>One thread on purpose. A run that overruns its period delays the next fire on this instance
 * instead of overlapping with it; overlap between instances is what the ShedLock lock prevents.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class SchedulingConfig {

  private static final String JOB_SCHEDULER = "jobTaskScheduler";

  @Bean(name = JOB_SCHEDULER, defaultCandidate = false)
  ThreadPoolTaskScheduler jobTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("jobs-");
    // Let the running job finish on shutdown so it releases its lock instead of leaving it held
    // until lockAtMostFor.
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(20);
    return scheduler;
  }

  @Bean
  SchedulingConfigurer jobScheduling(
      @Qualifier(JOB_SCHEDULER) ThreadPoolTaskScheduler jobTaskScheduler) {
    return registrar -> registrar.setTaskScheduler(jobTaskScheduler);
  }
}
