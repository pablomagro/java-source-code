import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Properties;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

class CloseWindow {

	static class EnumAllWindowNames {
	   static interface User32 extends StdCallLibrary {
	      User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

	      interface WNDENUMPROC extends StdCallCallback {
	         boolean callback(Pointer hWnd, Pointer arg);
	      }

	      boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);
	      int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
	      Pointer GetWindow(Pointer hWnd, int uCmd);
	   }

	   public static List<String> getAllWindowNames() {
	      final List<String> windowNames = new ArrayList<String>();
	      final User32 user32 = User32.INSTANCE;
	      user32 .EnumWindows(new User32.WNDENUMPROC() {

	         @Override
	         public boolean callback(Pointer hWnd, Pointer arg) {
	            byte[] windowText = new byte[512];
	            user32.GetWindowTextA(hWnd, windowText, 512);
	            String wText = Native.toString(windowText).trim();
	            if (!wText.isEmpty()) {
	               windowNames.add(wText);
	            }
	            return true;
	         }
	      }, null);

	      return windowNames;
	   }
	}

	// Inner class PropertiesFile.
	static class PropertiesFile {

		private static Properties prop = null;
		private InputStream input = null;
		private String PROPERTIES_FILE = "config.properties";

		PropertiesFile() {
			prop = new Properties();
			try {
				input = new FileInputStream(PROPERTIES_FILE);

				// load a properties file
				prop.load(input);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}		
		}

		public static Properties getConfigFile() {
			if (prop == null) {
				new PropertiesFile();
			}
			return prop;
		}
	}


	// Inner class OS.
	static class OS {
		// Current running OS.
		private static String strOS = System.getProperty("os.name").toLowerCase();

		public static String getOS() {
			return strOS;
		}

		public static boolean isWindows() {
			return (strOS.indexOf("win") >= 0);
		}

		public static boolean isMac() {
			return (strOS.indexOf("mac") >= 0);
		}

		public static boolean isUnix() {
			return (strOS.indexOf("nix") >= 0 || strOS.indexOf("nux") >= 0 || strOS.indexOf("aix") > 0 );		
		}

		public static boolean isSolaris() {
			return (strOS.indexOf("sunos") >= 0);
		}
	}

	// Command to run depending of the OS.
    private static String command = "";

    private static final String KILL_WIN = "taskkill.exe /F /FI \"WINDOWTITLE eq ";
    private static final String KILL_UNIX = "killall ";

    /*
     * Get the processses list using the parmater <code>strCommand</code> as OS command.
	 */
	public static ArrayList<String> getProcessesList(String strCommand) {
		ArrayList<String> temp = new ArrayList<String>();
		BufferedReader input = null;
	
		try {
		    String line;
		    Process p = Runtime.getRuntime().exec(strCommand);
		    input = new BufferedReader(new InputStreamReader(p.getInputStream()));
	
		    while ((line = input.readLine()) != null) {
		        temp.add(line.trim());
		    }
	
		    if (input != null)
				input.close();
		} catch (Exception err) {
		    err.printStackTrace();
		} finally {			
		}
	
		return temp;
    }

    /*
     * Search value <code>searchValue</code> into the array <code>elements</code>.
     * Return true  Element found.
     *        false Otherwise.
	 */
    private static boolean isElementFound(String[] elememts, String searchValue) {
    	for (int i = 1; i < elememts.length; i++) {
    		if (elememts[i].toLowerCase().contains(searchValue.toLowerCase())) {
    			return true;
    		}
    	}
    	return false;
    }

    /*
     * Kill all running programmes within the array <code>closeWindowsList</code>.
	 */
    public static void killProcess(List<String> winNameList, String[] closeWindowsList) {
    	if (winNameList == null || closeWindowsList.length == 0)
    		return;

    	// Transform the processes list ArrayLis to array.
    	String[] aux = winNameList.toArray(new String[winNameList.size()]);
    	boolean found = false;

    	for (int i = 0; i < closeWindowsList.length; i++) {
    		found = isElementFound(aux, closeWindowsList[i]);

	    	if (CloseWindow.OS.isWindows()) {
				command = KILL_WIN + closeWindowsList[i] + "*\"";
			} else {
				command = KILL_UNIX + closeWindowsList[i];
			}
			//System.out.println("Kill command '" + command + "'");

    		if (found) {
    			System.out.println("Is the window name '" + closeWindowsList[i] + "' running? : " + found);
    			killProcess(command);
    		}
    	}
    }

    /*
     * Call to the command <code>command</code> to kill the process <code>serviceName</code>.
	 */
    public static void killProcess(String command, String serviceName) {
    	try {
    		//System.out.println("Kill command '" + command + "' and windows name '" + serviceName + "'");
    		Runtime.getRuntime().exec(command + serviceName);  			
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
 	}

 	public static void killProcess(String command) {
 		killProcess(command, "");    	
 	}

    private static void executeProcess() {
    	// Init the instance command.
    	command = "";
    	List<String> winNameList = CloseWindow.EnumAllWindowNames.getAllWindowNames();

		if (CloseWindow.OS.isWindows()) {
			command = "tasklist.exe";
			winNameList = CloseWindow.EnumAllWindowNames.getAllWindowNames();
		}
		else if (CloseWindow.OS.isMac() || CloseWindow.OS.isUnix() || CloseWindow.OS.isSolaris()) {
			command = "ps -e";
			winNameList = getProcessesList(command);
		}
		else {
			System.out.println("Your OS is not support!!");
			System.exit(1);
		}

    	// Local lists.
    	String[] closeWindowsList = CloseWindow.PropertiesFile.getConfigFile().getProperty("windows.list").trim().split("\\,", -1);

    	killProcess(winNameList, closeWindowsList);
    }

    /*
     * Start the applications, and set the timer period twiked from the config.properties file,
     * and close (if there are/is running) allwindows specified into the parameter <code>windows.list</code>
     * within the above properties file.
     *
     * see @http://docs.oracle.com/javase/7/docs/api/java/util/Timer.html#scheduleAtFixedRate(java.util.TimerTask,%20java.util.Date,%20long)
	 */
    public static void main (String args[]) {
    	// Create a new Timer object.
    	Timer timer = new Timer();

    	// Set the timer period.
    	long period = Long.parseLong( CloseWindow.PropertiesFile.getConfigFile().getProperty("period").trim() );    

    	System.out.println("Operating System: " + CloseWindow.OS.getOS());	

    	// task - task to be scheduled.
		// firstTime - First time at which task is to be executed.
		// period - time in milliseconds between successive task executions.

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
		  	public void run() {
		  		// Print the current OS.
    			System.out.println("Current system time: " + new java.util.Date(System.currentTimeMillis()));
		  		
    			// Start the program ...
		    	executeProcess();
		  	}
		}, 1*1*1000, period);
    }

} 
