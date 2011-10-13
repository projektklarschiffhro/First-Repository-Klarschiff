package de.fraunhofer.igd.klarschiff.service.statistic;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;

public class StatusVerteilungEntry {
	EnumVorgangStatus status;
	long count;
	long ratio;
	
	protected StatusVerteilungEntry(Object[] values) {
		status = (EnumVorgangStatus)values[0];
		count = (Long)values[1];
	}
	protected void setCountOverall(long countOverall) {
		ratio = Math.round(100d/countOverall*count);
	}
	
	public EnumVorgangStatus getStatus() {
		return status;
	}
	public long getCount() {
		return count;
	}
	public long getRatio() {
		return ratio;
	}
}
