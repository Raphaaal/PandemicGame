package fr.dauphine.ja.pandemiage.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AiLoader {

	/**
	 * Load and returns the AI included in the jarfile
	 * ! It must exist a constructor without argument
	 * @param fileJarName name of the jar file
	 * @return the AI
	 */
	public static AiInterface loadAi(String fileJarName) {
		Path p = Paths.get(fileJarName);
		AiInterface aii = null;
		String AIClassName = null;

		try (JarFile jarFile = new JarFile(fileJarName);
				URLClassLoader loader = new URLClassLoader(new URL[] { p.toUri().toURL() })) {

			Manifest manifest = jarFile.getManifest();
			AIClassName = manifest.getMainAttributes().getValue("AI-Class");
			Class<? extends AiInterface> AIClass = loader.loadClass(AIClassName).asSubclass(AiInterface.class);
			aii =  AIClass.getConstructor().newInstance();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Couldn't find " + AIClassName + " in " + fileJarName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Errors loading " + fileJarName);
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("couldn't instanciate " + AIClassName);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} 


		return aii;
	}

}
