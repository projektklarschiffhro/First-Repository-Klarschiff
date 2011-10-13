package de.fraunhofer.igd.klarschiff.service.cluster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ScheduledSyncInCluster {
	String cron();
	int truncateField() default Calendar.MINUTE;
	String name() default "";
}
