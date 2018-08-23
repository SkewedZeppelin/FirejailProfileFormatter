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
import java.util.*;

public class PrivateEtcGenerator {

    private static final File profiles = new File("/home/tad/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles");
    private static final File profilesNew = new File("/home/tad/Development/Java/IntelliJ_Workspace/FirejailProfileFormatter/profiles-new");

    public static void main(String[] args) {
        File[] allProfiles = profiles.listFiles();
        for (File profile : allProfiles) {
            fix(profile);
        }
    }

    private static void fix(File profile) {
        String[] profileSplit = profile.toString().split("/");
        String profileName = profileSplit[profileSplit.length - 1].replaceAll(".profile", "").toLowerCase();
        File profileNew = new File(profilesNew.getPath() + "/" + profileSplit[profileSplit.length - 1]);

        System.out.println("\tProcessing " + profileName);

        try {
            boolean hasNetworking = true;
            boolean hasSound = true;
            boolean hasGui = true;
            boolean has3d = true;
            boolean hasDbus = true;
            boolean hasGroups = true;
            boolean hasAllusers = false;

            boolean hasSpecialIgnore = false;
            boolean hasSpecialTor = false;
            boolean hasSpecialJava = false;
            boolean hasSpecialMono = false;
            boolean hasSpecialSword = false;
            boolean hasSpecialSelf = false;

            ArrayList<String> rebuiltProfile = new ArrayList<>();
            Scanner profileReader = new Scanner(profile);
            while (profileReader.hasNext()) {
                String line = profileReader.nextLine();
                rebuiltProfile.add(line);

                if (line.equals("nosound")) {
                    hasSound = false;
                }
                if (line.equals("net none") || line.equals("protocol unix")) {
                    hasNetworking = false;
                }
                if (line.equals("quiet") || line.equals("noblacklist /sbin") || line.equals("private") || line.equals("blacklist /tmp/.X11-unix") || line.equals("x11 none")) {
                    hasGui = false;
                }
                if (line.equals("no3d")) {
                    has3d = false;
                }
                if (line.equals("nodbus")) {
                    hasDbus = false;
                }
                if (line.equals("nogroups")) {
                    hasGroups = false;
                }
                if (line.equals("allusers")) {
                    hasAllusers = true;
                }
                if (line.contains("private-") && line.contains("tor")) {
                    hasSpecialTor = true;
                }
                if (line.contains("private-") && line.contains("java") || line.equals("noblacklist ${PATH}/java") || line.equals("noblacklist ${HOME}/.java")) {
                    hasSpecialJava = true;
                }
                if (line.contains("private-") && line.contains("mono") || line.equals("noblacklist ${HOME}/.config/mono")) {
                    hasSpecialMono = true;
                }
                if (line.contains("private-etc") && line.contains("sword") || line.equals("noblacklist ${HOME}/.sword")) {
                    hasSpecialSword = true;
                }
                if (line.contains("private-etc") && (profileName.length() >= 3 && line.contains(profileName))) {
                    hasSpecialSelf = true;
                }
                if (line.contains("Redirect")) {
                    hasSpecialIgnore = true;
                }
            }
            profileReader.close();
            System.out.println("\t\tRead profile");

            String generatedEtc = generateEtc(hasNetworking, hasSound, hasGui, has3d, hasDbus, hasGroups, hasAllusers);
            if (generatedEtc.length() > 0 && !hasSpecialIgnore) {
                if (hasSpecialTor) {
                    generatedEtc += ",tor";
                }
                if (hasSpecialJava) {
                    generatedEtc += ",java*";
                }
                if (hasSpecialMono) {
                    generatedEtc += ",mono";
                }
                if (hasSpecialSword) {
                    generatedEtc += ",sword*";
                }
                if (hasSpecialSelf) {
                    generatedEtc += "," + profileName;
                }

                boolean addedNewEtc = false;
                PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
                String lastLine = "";
                for (String newLine : rebuiltProfile) {
                    if ((newLine.contains("private-etc") || lastLine.equals("private-dev"))) {
                        if(!addedNewEtc) {
                            profileOut.println(generatedEtc);
                            addedNewEtc = true;
                        }
                        if(lastLine.equals("private-dev") && !newLine.contains("private-etc")) {
                            profileOut.println(newLine);
                        }
                    } else {
                        profileOut.println(newLine);
                    }
                    lastLine = newLine;
                }
                if (!addedNewEtc) {
                    profileOut.println(generatedEtc);
                }
                profileOut.close();
                System.out.println("\t\tWrote profile");
            } else {
                System.out.println("\t\tIgnoring");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateEtc(boolean hasNetworking, boolean hasSound, boolean hasGui, boolean has3d, boolean hasDbus, boolean hasGroups, boolean hasAllusers) {
        String privateEtc = "private-etc ";
        Set<String> etcContents = new HashSet<>();

        etcContents.add("ld.so.*");
        etcContents.add("locale*");
        etcContents.add("localtime");
        //etcContents.add("magic*");
        etcContents.add("alternatives");
        etcContents.add("mime.types");
        etcContents.add("xdg");

        etcContents.add("os-release");
        etcContents.add("lsb-release");

        etcContents.add("passwd");
        etcContents.add("selinux");

        //TODO Handle the following: mtab, smb.conf, samba, cups, adobe, mailcap

        if (hasNetworking) {
            etcContents.add("ca-certificates");
            etcContents.add("ssl");
            etcContents.add("pki");
            etcContents.add("crypto-policies");
            etcContents.add("nssswitch.conf");
            etcContents.add("resolv.conf");
            etcContents.add("host*");
            etcContents.add("protocols");
            etcContents.add("services");
            etcContents.add("rpc");
            //etcContents.add("gai.conf");
            //etcContents.add("proxychains.conf");
        }
        if (hasSound) {
            etcContents.add("alsa");
            etcContents.add("asound.conf");
            etcContents.add("pulse");
            etcContents.add("machine-id");
        }
        if (hasGui) {
            etcContents.add("fonts");
            etcContents.add("dconf");
            etcContents.add("gtk*");
            etcContents.add("kde*rc");
            etcContents.add("pango");
            etcContents.add("X11");
        }
        if (has3d) {
            etcContents.add("drirc");
            etcContents.add("bumblebee");
            //etcContents.add("nvidia");
        }
        if (hasDbus) {
            etcContents.add("dbus-1");
            etcContents.add("machine-id");
        }
        if (hasGroups) {
            etcContents.add("group");
        }
        if (hasAllusers) {
            etcContents.add("passwd");
        }

        privateEtc += generateEtctSD(etcContents);

        //WORKAROUND NO PRIVATE-ETC GLOBBING
        privateEtc = privateEtc.replace("ld.so.*", "ld.so.cache,ld.so.preload,ld.so.conf,ld.so.conf.d");
        privateEtc = privateEtc.replace("locale*", "locale,locale.alias,locale.conf");
        privateEtc = privateEtc.replace("magic*", "magic,magic.mgc");
        privateEtc = privateEtc.replace("host*", "hosts,host.conf,hostname");
        privateEtc = privateEtc.replace("gtk*", "gtk-2.0,gtk-3.0");
        privateEtc = privateEtc.replace("kde*rc", "kde4rc,kde5rc");

        return privateEtc;
    }

    private static String generateEtctSD(Set<String> contents) {//Sort and delimit
        String sorted = "";

        ArrayList<String> contentSorted = new ArrayList<String>();
        contentSorted.addAll(contents);
        Collections.sort(contentSorted);

        for (String content : contentSorted) {
            sorted += "," + content;
        }
        sorted = sorted.substring(1, sorted.length()); //Remove first comma

        return sorted;
    }
}
