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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainN {

    private static File profiles = null;
    private static File profilesNew = null;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please supply an absolute directory containing: profiles, profiles-new");
            System.exit(1);
        }
        if(!args[0].endsWith("/")) {
            args[0] += "/";
        }
        profiles = new File(args[0] + "profiles/");
        profilesNew = new File(args[0] + "profiles-new/");

        File[] allProfiles = profiles.listFiles();
        for (File profile : allProfiles) {
            fix(profile);
        }
    }

    //Sorts a new option
    private static void fix(File profile) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            boolean hasOpt = false;
            Scanner profileReader = new Scanner(profile);
            while (profileReader.hasNext()) {
                String line = profileReader.nextLine();
                lines.add(line);
                if (line.equals("private-dev")) {
                    hasOpt = true;
                }
            }
            profileReader.close();

            if (hasOpt) {
                boolean added = false;
                String[] profileSplit = profile.toString().split("/");
                String profileName = profileSplit[profileSplit.length - 1].replaceAll(".profile", "");
                File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);
                PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
                String lastLine = "";
                for (String s : lines) {
                    if (!added) {
                        if (lastLine.startsWith("notv") || s.startsWith("novideo")) {
                            profileOut.println("nou2f");
                            added = true;
                        }
                    }
                    if (!s.startsWith("nou2f")) {
                        profileOut.println(s);
                        lastLine = s;
                    }
                }
                if (!added) {
                    profileOut.println("FIXME");
                }
                profileOut.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
