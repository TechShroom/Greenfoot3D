package k.greenfoot3d.zj;

public class ZipJar {

	public static String getZipOrJar(String s) {
		if (s.indexOf(".jar") > -1 || s.indexOf(".zip") > -1) {

		} else {
			System.err
					.println("[WARNING] Attempt to get zip or jar but path doesn't have either!");
		}
		return s;
	}

}
