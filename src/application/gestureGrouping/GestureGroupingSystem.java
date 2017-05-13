package application.gestureGrouping;

import java.util.Properties;

import javax.annotation.Nonnull;

import generalfactory.Factory;

public class GestureGroupingSystem {

	private GestureGroupingSystem() {
	}

	private static final @Nonnull String GESTURE_GROUPING_IMPL_PATH = "gesture.grouping.impl.path";
	private static final @Nonnull String GESTURE_GROUPING_IMPL_NAME = "gesture.grouping.impl.name";

	public static IGestureGrouper getGestureGrouper(Properties properties) throws Exception {

		String gestureGrouperClassName = properties.getProperty(GestureGroupingSystem.GESTURE_GROUPING_IMPL_NAME);
		String gestureGrouperPath = properties.getProperty(GestureGroupingSystem.GESTURE_GROUPING_IMPL_PATH);

		return Factory.getInstance(gestureGrouperClassName, gestureGrouperPath);
	}
	
}
