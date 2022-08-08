# Coub downloader - download all your likes at once!
This utility will help you to backup all your liked videos from site coub.com in bulk. No reincoding, no one-by-one saving.
Just run this once and save everything to your hard drive.
## Usage
### Requirements
* JAVA 8+
* Access token for coub.com
### Run
You need to find your access token in your browser's cookies. The cookie has name `remember_token` and contains 40 symbols.<br>
When you have it, you can run the util like this: `java -jar CoubDownloader.jar --token xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`, 
where X's are your access token.<br>
For each coub, the subfolder will be created named after the coub' name. It will contain original audio and video from the site.
### Options and examples
* By default coubs will be saved in the current folder. You can add param `--folder` with full path to specify where the coubs will be saved
* If you have trouble with folder names, add param `--sanitize_mode LATIN_ONLY`. Thus all non-latinic symbols will be removed from the folder's name.

Complete example:<br>
`java -jar CoubDownloader.jar --token xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx --folder D:\data\coubs --sanitize_mode LATIN_ONLY`<br>
or simply<br>
`java -jar CoubDownloader.jar -t xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -f D:\data\coubs -s LATIN_ONLY`
### Build from sources
Run `./gradlew buildFatJar` from project root directory. 
