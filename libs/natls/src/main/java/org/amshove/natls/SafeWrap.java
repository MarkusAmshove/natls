package org.amshove.natls;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafeWrap
{
	private static final Logger log = Logger.getAnonymousLogger();

	private SafeWrap()
	{}

	public static <R> CompletableFuture<R> wrapSafe(Supplier<CompletableFuture<R>> function)
	{
		return function.get().handle((r, e) ->
		{
			if (e != null)
			{
				log.log(Level.SEVERE, "Uncaught exception", e);
				return null;
			}

			return r;
		});
	}

	public static void wrapSafe(Runnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Uncaught exception", e);
		}
	}
}
