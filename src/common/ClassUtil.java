package common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.security.auth.login.LoginException;

public class ClassUtil {
	
	/**<pre>
	 * 获取指定包下的所有Class类
	 * 如果程序通过eclipse打包的话，要勾上Add directory entries
	 * </pre>
	 * @param packStr 包路径字符串，不支持默认包名(即java文件直接在src下)
	 * @param recursive 是否递归子包，为true的话将获取所有子包下的Class类
	 * @param subStrArr 该参数用于优化使用，当可以确定Class在只存在某些有规范的包名下时，可以指定这些包名，参见示例调用。如果没有或不了解该参数作用的话，忽略该参数即可
	 * @return 返回Set对象，保存扫描到的Class类
	 * @throws IOException 
	 */
	public static Set<Class<?>> getAllClassByPackStr(String packStr, boolean recursive, String... subStrArr) throws Exception {
		// 保存所有Class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 获取包的名字 并进行替换
		String packageName = packStr;
		String packageDirName = packageName.replace('.', '/');
		Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
		// 参考代码使用while，目测应该可以使用if的，出现问题再来看这里
		if (dirs.hasMoreElements()) {
			URL url = dirs.nextElement();
			String protocol = url.getProtocol(); // 得到协议的名称
			// 未打包的环境下处理
			if ("file".equals(protocol)) {
				// 获取包的物理路径
				String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				// 以文件的方式扫描整个包下的文件 并添加到集合中
				findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes, subStrArr);
			}
			// 打包的环境下处理
			else if ("jar".equals(protocol)) {
				// 获取jar
				JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
				findAndAddClassesInPackageByJar(jar, packageDirName, recursive, classes, subStrArr);
			} else {
				throw new LoginException(protocol + "不存在该协议名");
			}
		}
		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes, String... subStrArr) throws Exception {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes, subStrArr);
			}
			// 如果是文件
			else {
				boolean flag = subStrArr.length == 0;
				for (String subStr : subStrArr) {
					if (packageName.endsWith(subStr)) {
						flag = true;
						break;
					}
				}
				if (flag) {
					// 如果是java类文件 去掉后面的.class 只留下类名
					String className = file.getName().substring(0, file.getName().length() - 6);
					try {
						classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
					} catch (ClassNotFoundException e) {
						throw new Exception(e);
					}
				}
			}
		}
	}

	/**
	 * 以jar的形式来获取包下的所有Class
	 */
	private static void findAndAddClassesInPackageByJar(JarFile jar, String packageDirName, boolean recursive, Set<Class<?>> classes, String... subStrArr) throws Exception{
		packageDirName += "/";
		int sidx = packageDirName.lastIndexOf('/');
		Enumeration<JarEntry> entries = jar.entries();
		// 这里遍历jar下的每一层级所有文件，不止是指定包下
		while (entries.hasMoreElements()) {
			// 获取jar里的一个实体，可以是目录和一些jar包里的其他文件 如META-INF等文件
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			// 如果是以/开头的，注：参考代码有这个判断，但是观察输出的文件名，不存在/开头的
			if (name.charAt(0) == '/') {
				System.err.println("name.charAt(0) = " + name);
			}
			// 文件名前缀和指定的包名一致，即匹配到指定包
			if (name.startsWith(packageDirName)) {
				int idx = name.lastIndexOf('/');
				String packageName = name.substring(0, idx).replace('/', '.');
				if (sidx == idx || recursive) {
					boolean flag = subStrArr.length == 0;
					for (String subStr : subStrArr) {
						if (packageName.endsWith(subStr)) {
							flag = true;
							break;
						}
					}
					if (flag && name.endsWith(".class")) {
						String className = name.substring(packageName.length() + 1, name.length() - 6);
						try {
							classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
						} catch (ClassNotFoundException e) {
							throw new Exception();
						}
					}
				}
			}
		}
	}

}
