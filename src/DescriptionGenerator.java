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

public class DescriptionGenerator {

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
        System.out.println("Found " + allProfiles.length + " profiles");
        for (File profile : allProfiles) {
            addDescriptionIfAvailable(profile);
        }
    }

    private static void addDescriptionIfAvailable(File profile) {
        String[] profileSplit = profile.toString().split("/");
        String profileName = profileSplit[profileSplit.length - 1].replaceAll(".profile", "").toLowerCase();
        File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);

        System.out.println("\tProcessing " + profileName);

        //String description = getDescriptionFromApt(profileName);
        String description = getDescriptionFromPacman(profileName);
        if (description.length() > 0) {
            System.out.println("\t\tGot description: " + description);
            try {
                ArrayList<String> rebuiltProfile = new ArrayList<>();
                Scanner profileReader = new Scanner(profile);
                boolean containsDesc = false;
                while (profileReader.hasNext()) {
                    String line = profileReader.nextLine();
                    if(line.startsWith("# Description: ")) {
                        containsDesc = true;
                    }
                    if (line.toLowerCase().contains("this file is overwritten") && !containsDesc) {
                        rebuiltProfile.add("# Description: " + description);
                    }
                    rebuiltProfile.add(line);
                }
                profileReader.close();
                System.out.println("\t\tRead profile");

                if (!containsDesc) {
                    PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
                    for (String newLine : rebuiltProfile) {
                        profileOut.println(newLine);
                    }
                    profileOut.close();
                    System.out.println("\t\tWrote profile");
                } else {
                    System.out.println("\t\tProfile already contains a description");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("\t\tNo description available");
        }
    }

    private static String getDescriptionFromPacman(String packageName) {
        String description = "";
        try {
            Process getDescriptionFromPacman = new ProcessBuilder("pacman", "-Si", packageName).start();
            Scanner pacmanOutput = new Scanner(getDescriptionFromPacman.getInputStream());
            String line = "";
            while (pacmanOutput.hasNextLine()) {
                line = pacmanOutput.nextLine();
                if (line.startsWith("Description")) {
                    description = line.split(":")[1].trim();
                    break;
                }
            }
            pacmanOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

    private static String getDescriptionFromApt(String packageName) {
        String description = "";
        try {
            Process getDescriptionFromPacman = new ProcessBuilder("apt", "show", packageName).start();
            Scanner pacmanOutput = new Scanner(getDescriptionFromPacman.getInputStream());
            String line = "";
            while (pacmanOutput.hasNextLine()) {
                line = pacmanOutput.nextLine();
                if (line.startsWith("Description")) {
                    description = line.split(":")[1].trim();
                    break;
                }
            }
            pacmanOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

}
