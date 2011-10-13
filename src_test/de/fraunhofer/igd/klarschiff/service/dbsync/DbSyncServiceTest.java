package de.fraunhofer.igd.klarschiff.service.dbsync;

import org.junit.Test;


public class DbSyncServiceTest {

	@Test
	public void dbSyncTest() {
		DbSyncService dbSyncService = new DbSyncService();
		System.out.println(dbSyncService.syncDb());
	}
}
