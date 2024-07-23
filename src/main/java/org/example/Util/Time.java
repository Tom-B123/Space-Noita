package org.example.Util;

public class Time {
	public static float time_started = System.nanoTime();

	public static float get_time() { return (float)((System.nanoTime() - time_started) * 1E-9); }
}
