package com.edgelight;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgelight.LedManager.MODE;
import com.edgelight.ledconnector.SerialLedConnector;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import de.pitkley.jmccs.monitor.Monitor;
import de.pitkley.jmccs.monitor.MonitorHelper;
import de.pitkley.jmccs.monitor.MonitorManager;
import de.pitkley.jmccs.osx.OSXMonitorManager;
import de.pitkley.jmccs.win.WindowsMonitorManager;

public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);

	private LedManager ledManager;
	private MonitorHelper monitor;
	private int luminance = 30;

	private void setLuminanceSafe(int luminance) {
		synchronized(this) {
			if (monitor != null) {
				monitor.setLuminance(luminance);
			}
		}
	}

	public void run() throws Exception {
		TrayIcon trayIcon;
		SystemTray tray = SystemTray.getSystemTray();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Image image = Toolkit.getDefaultToolkit().getImage(classLoader.getResource("edgelight.png"));

		PopupMenu popup = new PopupMenu();

		MenuItem defaultItem = new MenuItem("Exit");
		defaultItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ledManager.setBrightness(0);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
				System.exit(0);
			}
		});
//	    java.awt.Font defaultFont = java.awt.Font.decode(null); // default font
//	    float adjustmentRatio = 1.5f; //  Calculate this based on your metrics
//	    float newFontSize = defaultFont.getSize() * adjustmentRatio ;
//	    java.awt.Font derivedFont = defaultFont.deriveFont(newFontSize);
//	    defaultItem.setFont(derivedFont);
		popup.add(defaultItem);

		Dimension trayIconSize = tray.getTrayIconSize();
		image = image.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
		trayIcon = new TrayIcon(image, "Edgelight", popup);

		tray.add(trayIcon);

		ledManager = new LedManager(new SerialLedConnector());
		ledManager.setMode(MODE.CHRISTMAS);
		ledManager.setBrightness(luminance);
		ledManager.start();

		MonitorManager monitorManager;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			monitorManager = new WindowsMonitorManager();
		} else {
			monitorManager = new OSXMonitorManager();
		}

		Monitor mon = monitorManager.getMainMonitor().get();
		while (mon == null) {
			logger.warn("No monitor found. Make sure it's connected through HDMI. Waiting...");
			Thread.sleep(3000);
			mon = monitorManager.getMainMonitor().get();
		}

		monitor = new MonitorHelper(mon);
		luminance = monitor.getLuminance().getCurrent();
		ledManager.setMode(MODE.DYNAMIC);
		ledManager.setBrightness(luminance);

		Provider provider = Provider.getCurrentProvider(true);
		provider.register(Configuration.HOTKEY_BRIGHTNESS_UP, new HotKeyListener() {
			@Override
			public void onHotKey(HotKey hotKey) {
				luminance = Math.min(100, luminance + Configuration.LUMINANCE_STEP);
				ledManager.setBrightness(luminance);
				setLuminanceSafe(luminance);
			}
		});
		provider.register(Configuration.HOTKEY_BRIGHTNESS_DOWN, new HotKeyListener() {
			@Override
			public void onHotKey(HotKey hotKey) {
				luminance = Math.max(0, luminance - Configuration.LUMINANCE_STEP);
				ledManager.setBrightness(luminance);
				setLuminanceSafe(luminance);
			}
		});
		provider.register(Configuration.HOTKEY_CHANGE_MODE, new HotKeyListener() {
			@Override
			public void onHotKey(HotKey hotKey) {
				ledManager.toggleMode();
			}
		});

		do {
			boolean monitorIsOff = monitor == null || (monitor.getLuminance().getCurrent() == 0 && luminance != 0);
			if (monitorIsOff) {
				logger.info("Monitor is off, searching for new one...");
				synchronized(this) {
					try {
						mon = monitorManager.getMainMonitor().orElse(null);
						monitor = mon == null ? null : new MonitorHelper(mon);
					} catch (Exception e) {
						monitor = null;
					}
				}
				logger.info(monitor == null ? "Not found." : "Found!");
				if (monitor != null) {
					luminance = monitor.getLuminance().getCurrent();
					ledManager.setBrightness(luminance);
				}
			}
			Thread.sleep(3000);
		} while (true);
	}

}
