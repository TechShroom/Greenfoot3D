package k.greenfoot3d.thread;

import k.greenfoot3d.world.World;
import crashcourse.k.library.lwjgl.DisplayLayer;

public class RunWorldThread extends Thread {

	private Class<? extends World> launch;
	private World launched = null;
	private String[] launchArgs;

	public RunWorldThread(Class<? extends World> app, String[] args) {
		launch = app;
		launchArgs = args;
	}

	public void run() {
		try {
			launched = launch.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (launched == null) {
			return;
		}
		launched.init(launchArgs);
		launched.start();
		DisplayLayer.destroy();
	}
}
