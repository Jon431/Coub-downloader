package com.coubdownloader.service;

import com.coubdownloader.classes.Coub;
import com.coubdownloader.classes.CoubException;
import com.coubdownloader.classes.FolderNameSanitizeMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpMethod.GET;

@Log4j2
public class DownloadService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int FIRST_PAGE = 1;
    private static final String PAGE_LINK = "https://coub.com/api/v2/timeline/likes?all=true&order_by=date&page=";
    private static final String COUB_LINK = "https://coub.com/view/";
    private static final String AUDIO_FILENAME = "audio.mp3";
    private static final String VIDEO_FILENAME = "video.mp4";


    public void downloadAllLikedCoubs(HttpHeaders headers, String rootFolder, FolderNameSanitizeMode sanitizeMode) {

        JsonNode page = getPage(FIRST_PAGE, headers);
        String totalPagesRawString = page.get("total_pages").asText();
        int totalPages = Integer.parseInt(totalPagesRawString);
        log.info("Found {} pages, starting to collect data..", totalPages);

        IntStream.range(FIRST_PAGE, totalPages)
                .mapToObj(pageIndex -> getCoubsFromOnePage(headers, pageIndex, sanitizeMode))
                .flatMap(Collection::stream)
                .forEach(coub -> downloadAndSave(coub, rootFolder));
    }

    private List<Coub> getCoubsFromOnePage(HttpHeaders headers, int pageIndex, FolderNameSanitizeMode sanitizeMode) {
        JsonNode page = getPage(pageIndex, headers).get("coubs");
        return IntStream.range(0, page.size())
                .mapToObj(coubIndex -> getOneCoub(page.get(coubIndex), sanitizeMode))
                .collect(toList());
    }

    private Coub getOneCoub(JsonNode jsonNode, FolderNameSanitizeMode sanitizeMode) {

        String idLink = jsonNode.get("permalink").textValue();

        String title = jsonNode.get("title").textValue();
        String sanitizedTitle = title.replaceAll(sanitizeMode.getRegex(), "").trim();

        JsonNode fileLinksNode = jsonNode.get("file_versions").get("html5");

        String videoUrl = fileLinksNode.path("video").path("higher").path("url").textValue();
        if (isEmpty(videoUrl)) {
            videoUrl = fileLinksNode.path("video").path("high").path("url").textValue();
        }

        String audioUrl = fileLinksNode.path("audio").path("high").path("url").textValue();
        if (isEmpty(audioUrl)) {
            audioUrl = fileLinksNode.path("audio").path("med").path("url").textValue();
        }
        return Coub.builder()
                .title(title)
                .sanitizedTitle(sanitizedTitle)
                .audioUrl(audioUrl)
                .videoUrl(videoUrl)
                .idLink(idLink)
                .url(COUB_LINK + idLink)
                .build();
    }

    private JsonNode getPage(int page, HttpHeaders headers) {
        try {
            return objectMapper.readTree(restTemplate.exchange(PAGE_LINK + page,
                    GET,
                    new HttpEntity<String>(headers),
                    byte[].class).getBody());
        } catch (Exception e) {
            throw new CoubException(format("Could not get coub page %s: ", page), e);

        }
    }

    private void downloadAndSave(Coub coub, String rootFolder) {

        log.info("Downloading coub: " + coub);

        Path savePath;
        try {
            savePath = Paths.get(rootFolder, coub.getSanitizedTitle() + format("_%s_",coub.getIdLink()));
            if (Files.exists(savePath)) {
                log.warn(format("Path %s already exist, using another name...", savePath));
                savePath = Paths.get(rootFolder, coub.getSanitizedTitle() + "(2)");
            }
        } catch (InvalidPathException e) {
            throw new CoubException(format("Cound not create path for coub: %s. " +
                    "Try to run the program with param sanitize_mode LATIN_ONLY", coub), e);
        }




        try {
            Files.createDirectories(savePath);
        } catch (IOException e) {
            throw new CoubException(format("Could not create dirs '%s' for coub '%s' (link %s):",
                    savePath, coub.getTitle(), coub.getUrl()), e);
        }

        try {
            if (isNotEmpty(coub.getAudioUrl())) {
                downloadAndSaveFile(coub.getAudioUrl(), savePath, AUDIO_FILENAME);
            }

            if (isNotEmpty(coub.getVideoUrl())) {
                downloadAndSaveFile(coub.getVideoUrl(), savePath, VIDEO_FILENAME);
            }
        } catch (Exception e) {
            throw new CoubException(format("Could not save coub '%s' (url %s):", coub.getTitle(), coub.getUrl()), e);
        }
    }

    private void downloadAndSaveFile(String url, Path savePath, String fileName) throws IOException {
        byte[] fileBytes = restTemplate.getForObject(url, byte[].class);
        assert fileBytes != null;
        Path videoPath = savePath.resolve(fileName);
        Files.write(videoPath, fileBytes);
    }
}
