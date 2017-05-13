package application.gestureGrouping;

import java.util.Properties;

import javax.annotation.Nonnull;

import application.system.ASystem;

public class GestureGroupingSystem extends ASystem{

	private GestureGroupingSystem() {
	}

	private static final @Nonnull String GESTURE_GROUPING_IMPL_PATH = "gesture.grouping.impl.path";
	private static final @Nonnull String GESTURE_GROUPING_IMPL_NAME = "gesture.grouping.impl.name";

	public static IGestureGrouper getGestureGrouper(Properties properties) throws Exception {
		return ASystem.getImplementation(properties, GestureGroupingSystem.GESTURE_GROUPING_IMPL_PATH,
				GestureGroupingSystem.GESTURE_GROUPING_IMPL_NAME);
	}
	
}
