import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainWNB {

    private static final File profiles = new File("/home/***REMOVED***/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles");
    private static final File profilesNew = new File("/home/***REMOVED***/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles-new");

    public static void main(String[] args) {
        File[] allProfiles = profiles.listFiles();
        for (File profile : allProfiles) {
            fix(profile);
        }
    }

    private static void fix(File profile) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            boolean hasWhitelist = false;
            Scanner profileReader = new Scanner(profile);
            while (profileReader.hasNext()) {
                String line = profileReader.nextLine();
                lines.add(line);
                if (line.startsWith("whitelist ${HOME}")) {
                    hasWhitelist = true;
                }
            }
            profileReader.close();

            if (hasWhitelist) {
                String[] profileSplit = profile.toString().split("/");
                File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);
                PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");


                ArrayList<String> blacklistPaths = new ArrayList<String>();
                for (String s : lines) {
                    if(s.startsWith("noblacklist")) {
                        blacklistPaths.add(s.split("noblacklist ")[1]);
                    }
                }

                boolean hadWhitelist = false;
                boolean wnbAdded = false;
                String lastLine = "";
                for (String s : lines) {
                    if(s.startsWith("whitelist")) {
                        hadWhitelist = true;
                    }

                    if(!wnbAdded && hadWhitelist && lastLine.replace("# ", "").startsWith("whitelist") && !s.replace("# ", "").startsWith("whitelist")) {
                        profileOut.println("whitelist-noblacklisted");
                        profileOut.println(s);
                        wnbAdded = true;
                    }else if(s.startsWith("whitelist")) {
                        if(!blacklistPaths.contains(s.split("whitelist ")[1])) {
                            profileOut.println(s);
                        }
                    } else {
                        profileOut.println(s);
                    }
                    lastLine = s;
                }
                profileOut.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
