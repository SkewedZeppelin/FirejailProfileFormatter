import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainN {

    private static File profiles = new File("/home/***REMOVED***/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles");
    private static File profilesNew = new File("/home/***REMOVED***/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles-new");

    public static void main(String[] args) {
        File[] allProfiles = profiles.listFiles();
        for(File profile : allProfiles) {
            fix(profile);
        }
    }

    public static void fix(File profile) {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            boolean hasOpt = false;
            Scanner profileReader = new Scanner(profile);
            while(profileReader.hasNext()) {
                String line = profileReader.nextLine();
                lines.add(line);
                if(line.equals("nodvd")) {
                    hasOpt = true;
                }
            }
            profileReader.close();

            if(hasOpt) {
                boolean added = false;
                String[] profileSplit = profile.toString().split("/");
                String profileName = profileSplit[profileSplit.length - 1].replaceAll(".profile", "");
                File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);
                PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
                for(String s : lines) {
                    if(!added) {
                        if(s.startsWith("nogroups") || s.startsWith("nonewprivs") || s.startsWith("noroot") ||s.startsWith("nosound") || s.startsWith("notv")) {
                            profileOut.println("nodvd");
                            added = true;
                        }
                    }
                    if(!s.startsWith("nodvd")) {
                        profileOut.println(s);
                    }
                }
                if(!added) {
                    profileOut.println("FIXME");
                }
                profileOut.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
