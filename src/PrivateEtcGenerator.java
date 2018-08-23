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
        String profileNameReduced = profileName.substring(0, profileName.length() - 1);
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
            boolean hasSpecialSelf = false;

            ArrayList<String> rebuiltProfile = new ArrayList<>();
            Scanner profileReader = new Scanner(profile);
            while (profileReader.hasNext()) {
                String line = profileReader.nextLine();
                rebuiltProfile.add(line);

                if(line.equals("nosound")) {
                    hasSound = false;
                }
                if(line.equals("net none") || line.equals("protocol unix")) {
                    hasNetworking = false;
                }
                if(line.equals("noblacklist /sbin") || line.equals("private") || line.equals("blacklist /tmp/.X11-unix")) {
                    hasGui = false;
                }
                if(line.equals("no3d")) {
                    has3d = false;
                }
                if(line.equals("nodbus")) {
                    hasDbus = false;
                }
                if(line.equals("nogroups")) {
                    hasGroups = false;
                }
                if(line.equals("allusers")) {
                    hasAllusers = true;
                }
                if(line.contains("private-bin") && line.contains("tor,")) {
                    hasSpecialTor = true;
                }
                if(line.contains("private-") && (profileNameReduced.length() >= 4 && line.contains(profileNameReduced))) {
                    hasSpecialSelf = true;
                }
                if(line.contains("# Redirect")) {
                    hasSpecialIgnore = true;
                }
            }
            profileReader.close();
            System.out.println("\t\tRead profile");

            String generatedEtc = generateEtc(hasNetworking, hasSound, hasGui, has3d, hasDbus, hasGroups, hasAllusers);
            if (generatedEtc.length() > 0 && !hasSpecialIgnore) {
                if(hasSpecialTor) {
                    generatedEtc += ",tor";
                }
                if(hasSpecialSelf) {
                    generatedEtc += "," + profileNameReduced + "*";
                }

                boolean addedNewEtc = false;
                PrintWriter profileOut = new PrintWriter(profileNew, "UTF-8");
                String lastLine = "";
                for (String newLine : rebuiltProfile) {
                    if(!addedNewEtc && newLine.contains("private-etc ")) {
                        profileOut.println(generatedEtc);
                        addedNewEtc = true;
                    } else {
                        if(!addedNewEtc && lastLine.equals("private-dev")) {
                            profileOut.println(generatedEtc);
                            addedNewEtc = true;
                        }
                        profileOut.println(newLine);
                    }
                    lastLine = newLine;
                }
                if(!addedNewEtc) {
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
        etcContents.add("localtime");
        etcContents.add("alternatives");
        etcContents.add("mime-types");
        etcContents.add("xdg");

        etcContents.add("os-release");
        etcContents.add("lsb-release");

        etcContents.add("passwd");
        etcContents.add("selinux");

        if(hasNetworking) {
            etcContents.add("ca-certificates");
            etcContents.add("ssl");
            etcContents.add("pki");
            etcContents.add("crypto-policies");
            etcContents.add("nssswitch.conf");
            etcContents.add("resolv.conf");
            etcContents.add("host*");
            etcContents.add("services");
            etcContents.add("gai.conf");
            //etcContents.add("proxychains.conf");
        }
        if(hasSound) {
            etcContents.add("alsa");
            etcContents.add("asound.conf");
            etcContents.add("pulse");
            etcContents.add("machine-id");
        }
        if(hasGui) {
            etcContents.add("fonts");
            etcContents.add("gtk*");
            etcContents.add("pango");
            etcContents.add("X11");
        }
        if(has3d) {
            etcContents.add("drirc");
            etcContents.add("bumblebee");
            etcContents.add("nvidia");
        }
        if(hasDbus) {
            etcContents.add("dbus-1");
        }
        if(hasGroups) {
            etcContents.add("group");
        }
        if(hasAllusers) {
            etcContents.add("passwd");
        }

        privateEtc += generateEtctSD(etcContents);

        return privateEtc;
    }

    private static String generateEtctSD(Set<String> contents) {//Sort and delimit
        String sorted = "";

        ArrayList<String> contentSorted = new ArrayList<String>();
        contentSorted.addAll(contents);
        Collections.sort(contentSorted);

        for(String content : contentSorted) {
            sorted += "," + content;
        }
        sorted = sorted.substring(1, sorted.length()); //Remove first comma

        return sorted;
    }
}
