package org.amshove.natls.config;

public interface IConfigChangedSubscriber
{
	void configChanged(LSConfiguration newConfig);
}
