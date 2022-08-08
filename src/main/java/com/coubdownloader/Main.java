package com.coubdownloader;

import com.coubdownloader.classes.CoubException;
import com.coubdownloader.classes.FolderNameSanitizeMode;
import com.coubdownloader.service.DownloadService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.Scanner;

import static com.coubdownloader.classes.FolderNameSanitizeMode.*;
import static java.lang.String.format;

@Log4j2
public class Main {
    private HttpHeaders headers;
    private String mainFolder;
    private FolderNameSanitizeMode sanitizeMode;

    private static final String TOKEN_OPT_NAME = "token";
    private static final String FOLDER_OPT_NAME = "folder";
    private static final String SANITIZE_MODE_OPT_NAME = "sanitize_mode";

    public static void main(String[] args) {
        Main main = new Main();
        if (args.length == 0) {
            main.fillParams();
        } else {
            main.parseArgs(args);
        }

        DownloadService downloadService = new DownloadService();
        downloadService.downloadAllLikedCoubs(main.headers, main.mainFolder, main.sanitizeMode);
    }

    private void fillParams() {
        log.error("Welcome to Coub like downloader utility. It will help to export all your liked videos from site coub.com");
        log.info("Please specify access token for coub.com:");

        Scanner scanner = new Scanner(System.in);
        String tokenValue = scanner.nextLine();

        headers = new HttpHeaders();
        headers.add("Cookie", "remember_token=" + tokenValue);

        log.info("Provide full path to folder for videos or press Enter to use current folder");
        String userPath = scanner.nextLine();
        if (StringUtils.isNotEmpty(userPath)) {
            mainFolder = userPath;
        } else {
            mainFolder = System.getProperty("user.dir");
        }

        log.info("Provide coub name sanitize mode or press Enter to use ALL_LETTERS");
        String sanitizeModeString = scanner.nextLine();
        sanitizeMode = getSanitizeMode(sanitizeModeString);
    }

    private void parseArgs(String[] args) {
        Options options = new Options();

        Option tokenOpt = new Option("t", TOKEN_OPT_NAME, true, "Access token for coub.com. " +
                "Can be found in browser cookies");
        tokenOpt.setRequired(true);
        options.addOption(tokenOpt);

        Option outputFolderOpt = new Option("f", FOLDER_OPT_NAME, true, "Full path to output folder");
        outputFolderOpt.setRequired(false);
        options.addOption(outputFolderOpt);

        Option sanitizeModeOpt = new Option("s", SANITIZE_MODE_OPT_NAME, true,
                format("Coub names sanitize mode. Default is %s. Possible values: %s, %s, %s",
                        ALL_LETTERS.name(), LATIN_ONLY.name(), LATIN_AND_CYRILLIC.name(), ALL_LETTERS.name()));
        sanitizeModeOpt.setRequired(false);
        options.addOption(sanitizeModeOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error(e.getMessage());
            formatter.printHelp("java -jar CoubDownloader.jar", options);
            System.exit(1);
        }

        if (cmd.hasOption(FOLDER_OPT_NAME)) {
            mainFolder = cmd.getOptionValue(FOLDER_OPT_NAME);
        } else {
            mainFolder = System.getProperty("user.dir");
        }

        if (cmd.hasOption(SANITIZE_MODE_OPT_NAME)) {
            sanitizeMode = getSanitizeMode(cmd.getOptionValue(SANITIZE_MODE_OPT_NAME));
        } else {
            sanitizeMode = ALL_LETTERS;
        }

        headers = new HttpHeaders();
        headers.add("Cookie", "remember_token=" + cmd.getOptionValue(TOKEN_OPT_NAME));

    }

    private FolderNameSanitizeMode getSanitizeMode(String sanitizeModeString) {
        if (StringUtils.isNotEmpty(sanitizeModeString)) {
            try {
                return FolderNameSanitizeMode.valueOf(sanitizeModeString);
            } catch (Exception e) {
                throw new CoubException(format("Could not parse sanitize mode '%s'! Possible values: %s, %s, %s",
                        sanitizeModeString, LATIN_ONLY, LATIN_AND_CYRILLIC, ALL_LETTERS), e);
            }
        } else {
            return ALL_LETTERS;
        }
    }
}
