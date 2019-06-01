/*This file is part of FirejailProfileFormatter.

  FirejailProfileFormatter is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 2 of the License, or
  (at your option) any later version.

  FirejailProfileFormatter is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with FirejailProfileFormatter.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final File profiles = new File("/home/tad/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles");
    private static final File profilesNew = new File("/home/tad/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles-new");

    public static void main(String[] args) {
        File[] allProfiles = profiles.listFiles();
        for (File profile : allProfiles) {
            rewriteProfile(profile);
        }
    }

    private static void rewriteProfile(File profile) {
        try {
            //Read the profile into an array
            Scanner profileReader = new Scanner(profile);
            boolean isWhitelist = false;
            boolean quiet = false;
            ArrayList<String> profileNoBlacklist = new ArrayList<>();
            ArrayList<String> profileIncludes = new ArrayList<>();
            ArrayList<String> profileWhitelist = new ArrayList<>();
            ArrayList<String> profileOptions = new ArrayList<>();
            ArrayList<String> profileOptionsPrivate = new ArrayList<>();
            ArrayList<String> profileOptionsMisc = new ArrayList<>();
            ArrayList<String> profileExtends = new ArrayList<>();
            ArrayList<String> profileComments = new ArrayList<>();
            int c = 0;
            while (profileReader.hasNext()) {
                c++;
                String line = profileReader.nextLine().trim();
                line = line.replaceAll("#([^\\s-])", "# $1");
                if (line.equals("quiet")) {
                    quiet = true;
                } else if (line.startsWith("blacklist") || line.startsWith("noblacklist")) {
                    profileNoBlacklist.add(line);
                } else if (line.startsWith("include disable-") || line.startsWith("# include disable-")) {
                    profileIncludes.add(line);
                } else if (line.startsWith("mkfile") || line.startsWith("mkdir") || line.startsWith("whitelist")) {
                    profileWhitelist.add(line);
                } else if (line.equals("include whitelist-common.inc")) {
                    isWhitelist = true;
                } else if (line.startsWith("private") || line.startsWith("disable-mnt") || line.startsWith("# private") || line.startsWith("read-")) {
                    profileOptionsPrivate.add(line);
                } else if (line.startsWith("noexec") || line.startsWith("memory-deny-write-execute")) {
                    profileOptionsMisc.add(line);
                } else if (line.startsWith("include")) {
                    if (line.contains(".profile")) {
                        profileExtends.add(line);
                    }
                } else if (line.startsWith("# Persistent") || line.startsWith("# This file") || line.startsWith("# Firejail") || line.contains("profile") && line.startsWith("#") || line.equals("") || line.startsWith("# Blacklist Paths") || line.startsWith("# No Blacklist Paths") || line.startsWith("# Options") || line.startsWith("# Profile") || line.startsWith("# ipc-namespace") || line.startsWith("# Whitelist") || line.startsWith("# ##") || line.replaceAll("\\s", "").equals("") || line.length() < 3 || line.equals("# silverlight") || line.contains("experimental") || line.startsWith("# Redirect")) {
                    //ignore
                } else {
                    if (line.startsWith("#")) {
                        profileComments.add(line);
                    } else {
                        profileOptions.add(line);
                    }
                }
            }
            profileReader.close();
            //System.out.println("Read " + c + " lines");

            //Rewrite the profile from the contents of the array
            String[] profileSplit = profile.toString().split("/");
            String profileName = profileSplit[profileSplit.length - 1].replaceAll(".profile", "");
            File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);
            PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
            //header XXX: VERIFY ALIAS HEADERS - grep "Firejail profile alias for" *.profile
            if (profileExtends.size() == 1 && !profileExtends.get(0).contains("default.profile") && !profileExtends.get(0).contains("electron.profile") && !profileExtends.get(0).contains("firefox.profile") && !(profileOptions.size() > 2)) {
                String alias = profileExtends.get(0).split(".profile")[0];
                profileOut.println("# Firejail profile alias for " + alias);
                profileOut.println("# This file is overwritten after every install/update\n");
            } else {
                profileOut.println("# Firejail profile for " + profileName);
                profileOut.println("# This file is overwritten after every install/update");
                if (quiet) {
                    profileOut.println("quiet");
                }
                profileOut.println("# Persistent local customizations");
                profileOut.println("include " + profileName + ".local");
                profileOut.println("# Persistent global definitions");
                profileOut.println("include globals.local\n");
            }
            //noblacklist
            if (profileNoBlacklist.size() > 0) {
                profileNoBlacklist.sort(String::compareTo);
                for (int x = 0; x < profileNoBlacklist.size(); x++) {
                    if (x > 0 && profileNoBlacklist.get(x - 1).startsWith("blacklist") && profileNoBlacklist.get(x).startsWith("noblacklist")) {
                        profileOut.println();
                    }
                    profileOut.println(profileNoBlacklist.get(x));
                }
            }
            //includes
            if (profileIncludes.size() > 0) {
                profileOut.println();
                profileIncludes.sort(Main::compareToIgnoreComment);
                for (String includes : profileIncludes) {
                    profileOut.println(includes);
                }
            }
            //whitelist
            if (profileWhitelist.size() > 0) {
                profileOut.println();
                profileWhitelist.sort(String::compareTo);
                for (String whitelist : profileWhitelist) {
                    profileOut.println(whitelist);
                }
                isWhitelist = true;
            }
            if (isWhitelist) {
                profileOut.println("include whitelist-common.inc");
            }
            //options
            if (profileOptions.size() > 0) {
                profileOut.println();
                profileOptions.sort(String::compareTo);
                for (String options : profileOptions) {
                    profileOut.println(options);
                }
            }
            //options private
            if (profileOptionsPrivate.size() > 0) {
                profileOut.println();
                profileOptionsPrivate.sort(Main::compareToIgnoreComment);
                for (String optionsPrivate : profileOptionsPrivate) {
                    profileOut.println(optionsPrivate);
                }
            }
            //options misc
            if (profileOptionsMisc.size() > 0) {
                profileOut.println();
                profileOptionsMisc.sort(String::compareTo);
                for (String optionsMisc : profileOptionsMisc) {
                    profileOut.println(optionsMisc);
                }
            }
            //alias
            if (profileExtends.size() > 0) {
                profileOut.println();
                profileExtends.sort(String::compareTo);
                for (String extend : profileExtends) {
                    profileOut.println(extend);
                }
            }
            //comments
            if (profileComments.size() > 0) {
                profileOut.println();
                profileComments.sort(String::compareTo);
                profileOut.println("# CLOBBERED COMMENTS");
                for (String comments : profileComments) {
                    profileOut.println(comments);
                }
            }
            //finish
            profileOut.close();
            System.out.println("Rewrote " + profileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int compareToIgnoreComment(String s1, String s2) {
        return s1.replaceAll("# ", "").compareTo(s2.replaceAll("# ", ""));
    }

}
