package net.mms_projects.copyit.sync_services;

import java.util.Date;

import net.mms_projects.copyit.ServiceInterface;

public interface PushServiceInterface extends ServiceInterface {

	public void activatePush();

	public void deactivatePush();

	public boolean isPushActivated();
	
	public void doPush(String content, Date date);

}