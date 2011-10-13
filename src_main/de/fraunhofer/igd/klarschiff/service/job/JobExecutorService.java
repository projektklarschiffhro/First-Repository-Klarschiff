package de.fraunhofer.igd.klarschiff.service.job;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

@Service
public class JobExecutorService {

	int corePoolSize = 2;
	int maxPooSize = 4;
	long keepAliveTime = 10;
	int workQueueSize = 30;
	
	ThreadPoolExecutor threadPool;
	ArrayBlockingQueue<Runnable> workQueue;
	
	@PostConstruct
	public void init() {
		workQueue = new ArrayBlockingQueue<Runnable>(workQueueSize);
		threadPool = new ThreadPoolExecutor(corePoolSize, maxPooSize, keepAliveTime, TimeUnit.MINUTES, workQueue);
	}
	
	@PreDestroy
	public void stop() {
		threadPool.shutdown();
	}
	
	public void runJob(Runnable job) {
		threadPool.execute(job);
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPooSize() {
		return maxPooSize;
	}

	public void setMaxPooSize(int maxPooSize) {
		this.maxPooSize = maxPooSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getWorkQueueSize() {
		return workQueueSize;
	}

	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}
	
	
}
