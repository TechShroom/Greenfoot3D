package k.greenfoot3d.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipFile;

import k.greenfoot3d.thread.RunWorldThread;
import k.greenfoot3d.ui.Inform;
import k.greenfoot3d.world.World;
import k.greenfoot3d.zj.ZipJar;

public class Greenfoot3D {

	public static String[] launchedArgs;
	public static ArrayList<Class<? extends World>> apps = new ArrayList<Class<? extends World>>();
	public static String topLevel;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setErr(new PrintStream(System.err, true));
		System.setProperty("sun.misc.URLClassPath.debug", "true");
		topLevel = Greenfoot3D.class.getResource("Greenfoot3D.class").getPath()
				.replace("k/greenfoot3d/main/Greenfoot3D.class", "");
		System.err.println("Our top level is " + topLevel);
		launchedArgs = args;
		findApps();
		launchApps();
	}

	private static void launchApps() {
		for (Class<? extends World> app : apps) {
			System.err.println("Launching app class " + app);
			Thread runWorld = new RunWorldThread(app, launchedArgs);
			runWorld.setDaemon(false);
			runWorld.start();
		}
	}

	private static void findApps() {
		System.err.println("Searching for a Greenfoot3D App...");
		System.err.println("java.class.path={");
		String[] splitArray = System.getProperty("java.class.path").split(
				File.pathSeparator);
		ArrayList<String> split = new ArrayList<String>(
				Arrays.asList(splitArray));
		for (String s : split) {
			if (!s.endsWith(File.separator)) {
				split.set(split.indexOf(s), (s += File.separator));
			}
			String out = s + ",";
			if (split.indexOf(s) == (split.size() - 1)) {
				out = s + "\n}";
			}
			System.err.println(out);
		}
		for (String s : split) {
			System.err.println("Reading path entry '" + s + "'");
			System.err
					.println("Attempting to get File instance for easy handling...");
			int method = -1; // possible: 0=file, 1=classpath (input stream),
								// 2=zip
			File f = null;
			InputStream is = null;
			ZipFile zf = null;
			try {
				f = new File(s);
				method = 0;
				System.err.println("Success. Procceding to scan...");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed, trying ZipFile...");
			}
			if (method == 0) {
				// File scan, super easy.
				int result = recursiveFileScan(f, f);
				method = result == 2 ? 3 : result == 1 ? method : -1;
			}
			if (method == -1) {
				System.err
						.println("Failed loading anything from scan, trying ZipFile...");
				try {
					zf = new ZipFile(ZipJar.getZipOrJar(s));
					method = 2;
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Failed, trying classpath...");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static int recursiveFileScan(File check, File started) {
		if (check.isDirectory()) {
			int ret = 0;
			for (File z : check.listFiles()) {
				int res = recursiveFileScan(z, started);
				if (res == 2) {
					ret = 2;
				} else {
					if (res == 1 && ret != 2) {
						ret = 1;
					}
				}
			}
			return ret;
		} else {
			if (check.getAbsolutePath().endsWith(".class")) {
				URL fURL = null, fURL2 = null;
				;
				try {
					fURL = check.toURI().toURL();
					fURL2 = started.toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
					System.err
							.println("Weird URL, cancelling this file's scan ("
									+ check.getAbsolutePath() + ")");
					return 2;
				}
				String name = fURL.getPath().replace(fURL2.getPath(), "")
						.replace('/', '.').replace(".class", "");
				System.err.println("Found class file named " + name
						+ "\nAttempting to load through URLClassLoader...");
				Class<?> claz = null;
				try {
					if (!fURL2.getPath().endsWith("/")) {
						fURL2 = new URL(fURL2.getProtocol(), fURL2.getHost(),
								fURL2.getFile() + "/");
					}
					System.err.println("fURL2 is " + fURL2.toString());
					claz = new URLClassLoader(new URL[] { fURL2 },
							Greenfoot3D.class.getClassLoader()).loadClass(name);
					System.err.println("Loaded. Checking for World...");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					System.err
							.println("Class not loadable by URLClassLoader. Will not load this class.");
					return 2;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					System.err.println("Bad URL!");
					return 2;
				}
				if (claz != null && claz != World.class
						&& World.class.isAssignableFrom(claz)) {
					System.err.println(name
							+ " is a World. Checking World constructor...");
					return checkWorldConstructors((Class<? extends World>) claz);
				} else if (claz != null) {
					System.err.println(name + " is not a World.");
					return 2;
				} else {
					System.err.println(name + " is not a class?");
				}
			}
			return 0;
		}
	}

	private static int checkWorldConstructors(Class<? extends World> claz) {
		try {
			Constructor<?> empty = claz.getConstructor();
			if ((empty.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC) {
				throw new IllegalAccessException();
			}
			apps.add(claz);
			System.err.println("Success!");
			return 1;
		} catch (SecurityException e) {
			Inform.userOf("Security problem!");
		} catch (NoSuchMethodException e) {
			Inform.userOf("Missing no-args world constructor!");
		} catch (IllegalAccessException e) {
			Inform.userOf("Blocked no-args world constructor!");
		}
		return 0;
	}
}
