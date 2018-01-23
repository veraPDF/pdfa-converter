package converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * @author Maksim Bezrukov
 */
public class CleanerService implements Runnable {

	private static final long SLEEP_DURATION = 4*60*60*1000;

	private static final long FILE_LIFETIME_IN_MILLIS = 3*24*60*60*1000;

	private final String tempPath;
	private boolean running;
	private String stopReason;

	public CleanerService(String tempPath) {
		this.running = false;
		this.tempPath = tempPath;
	}

	public boolean isRunning() {
		return running;
	}

	public String getStopReason() {
		return stopReason;
	}

	public void start() {
		if (!running) {
			running = true;
			stopReason = null;
			new Thread(this, "Thread-CleanerService").start();
		}
	}

	@Override
	public void run() {
		try {
			while (running) {
				if (onRepeat()) {
					Thread.sleep(SLEEP_DURATION);
				}
			}
		} catch (Throwable e) {
			this.stopReason = e.getMessage();
		} finally {
			running = false;
		}
	}

	private boolean onRepeat() {
		File tempFolder = new File(this.tempPath);
		if (tempFolder.isDirectory()) {
			long currentTimeInMillis = System.currentTimeMillis();
			for (File f : tempFolder.listFiles()) {
				checkFile(f, currentTimeInMillis);
			}
		}
		return true;
	}

	private void checkFile(File file, long currentTimeInMillis) {
		if (file.exists()) {
			if (file.isFile()) {
				try {
					BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					FileTime fileTime = attributes.creationTime();
					if (currentTimeInMillis - fileTime.toMillis() > FILE_LIFETIME_IN_MILLIS) {
						file.delete();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					checkFile(f, currentTimeInMillis);
				}
				if (file.listFiles().length == 0) {
					file.delete();
				}
			}
		}
	}
}
