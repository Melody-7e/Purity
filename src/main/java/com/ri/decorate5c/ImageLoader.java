package com.ri.decorate5c;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.WaitUntilState;
import com.ri.meta.LocalVariables;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;

import javax.imageio.ImageIO;

public class ImageLoader implements Closeable {
    public static final boolean OFFLINE_MODE  = false;
    public static final File    OFFLINE_FILES = new File(System.getProperty("user.home"), // or use Projects.PROJECT_DIR
                                                         "Desktop/Wallpaper/decorate5c_offlines/");

    public static final boolean HIGH_RES      = false;
    public static final float   MIN_ASPECT    = 0.3f;
    public static final float   MAX_ASPECT    = 0.9f;

    private static final String URL   = "https://in.pinterest.com/";
    private static final String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36";

    private final Playwright playwright;
    private final Browser    browser;
    private final Page       page;

    private final File             directory;
    private final Stack<ImagePath> imagePaths = new Stack<>();
    private final File[]           offlinePaths;

    private String        selectedUrl  = URL;
    private String        currentUrl   = URL;
    private boolean       loading      = false;
    private boolean       needRefresh  = true;
    private int           offlineIndex = 0;
    private String        cachedUrl;
    private BufferedImage cachedImage;
    private Thread        cachedThread;
    private boolean       cachingImage;

    public ImageLoader()
    {
        /*
         * For `LocalVariables`
         *
         * Go to `pinterest.com` and open devtools (Inspect Page, Shift+Ctrl+C) and go the network tab.
         * Refresh page and see the first requests (with name *.pinterest.com), in the `Request Headers` area find `Cookie`
         * Find _auth=...; and _pinterest_sess=...; in it and copy the values.
         * _auth is integer and _pinterest_sess is a base64 encoded data.
         * Write it in localVariables.txt with name decorate5c_auth and decorate5c_pinterest_sess respectively.
         * */
        String auth = Objects.requireNonNull(LocalVariables.get("decorate5c_auth"));
        String sess = Objects.requireNonNull(LocalVariables.get("decorate5c_pinterest_sess"));

        try {
            directory = Files.createTempDirectory("Decorate5c").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                if (directory.exists()) {
                    Files.delete(directory.toPath());
                }
            } catch (IOException err) {
                err.printStackTrace();
            }

            uncaughtExceptionHandler.uncaughtException(t, e);
        });

        ArrayList<File> offlineFiles = new ArrayList<>(Arrays.stream(Objects.requireNonNull(OFFLINE_FILES.listFiles()))
                                                      .filter(p -> p.getName().matches(".*\\.(png|jpg|jpeg|webp|gif)"))
                                                      .toList());
        Collections.shuffle(offlineFiles);
        offlinePaths = offlineFiles.toArray(new File[0]);

        if (!OFFLINE_MODE) {
            playwright = Playwright.create();
            browser    = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext context = browser.newContext();
            context.addCookies(Arrays.asList(
                    new Cookie("_auth", auth).setDomain(".pinterest.com").setPath("/"),
                    new Cookie("_pinterest_sess", sess).setDomain(".pinterest.com").setPath("/")
            ));

            page = context.newPage();
        } else {
            playwright = null;
            browser    = null;
            page       = null;
        }

        nextImage();
    }

    public void fetchImages() {
        if (needRefresh) {
            needRefresh = false;
            page.navigate(
                    selectedUrl,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
            );
            page.waitForTimeout(4000);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> data = (List<Map<String, String>>) page.evaluate(
                """
                () => {
                  const images = Array.from(document.querySelectorAll('img'));
                
                  return images.map(img => {
                      const parentAnchor = img.closest('a');
                      const pinContainer = img.closest('[data-grid-item="true"]');
                      const linkHref     = parentAnchor ? parentAnchor.href : null;
                      const imgSrc       = img.src;
                
                      img.remove();
                
                      return {imgSrc: imgSrc, linkHref: linkHref};
                  });
                }"""
        );
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");

        System.out.println("saving");
        for (Map<String, String> item: data) {
            try {
                String img  = item.get("imgSrc");
                String link = item.get("linkHref");

                if (link == null) continue;

                if (img.contains("pinimg.com")) {
                    if (HIGH_RES) img = img.replace("/236x/", "/736x/"); // for some reason /originals/ don't always load
                    else img = img.replace("/236x/", "/474x/");
                }

                URL           url        = new URI(img).toURL();
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", AGENT);

                BufferedImage bufferedImage = ImageIO.read(connection.getInputStream());

                float aspect = (float) bufferedImage.getWidth()/bufferedImage.getHeight();
                if (aspect < MIN_ASPECT || aspect > MAX_ASPECT) continue;

                File output = new File(
                        directory,
                        link.replace("https://", "").replaceAll("['\"/\\\\<>|]", "-") + ".png"
                );
                ImageIO.write(
                        bufferedImage, "png", output
                );

                imagePaths.add(new ImagePath(output, link));

                System.out.println("Image: " + img + " | Linked to: " + link);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        System.out.println("done");
        loading = false;
    }

    @Override
    public void close()
    {
        browser.close();
        playwright.close();
    }

    boolean isOnline() {
        return currentUrl != null;
    }

    boolean isDownloaded() {
        if (currentUrl == null) return true;
        File file = new File(
                OFFLINE_FILES,
                currentUrl.replace("https://", "").replaceAll("['\"/\\\\<>|]", "-") + ".png"
        );
        return file.exists();
    }

    int numImages() {
        return imagePaths.size() /*+ (cachedUrl != null ? 1 : 0)*/;
    }

    boolean select() {
        needRefresh = true;
        if (currentUrl != null) selectedUrl = currentUrl;
        return currentUrl != null;
    }

    void unselect() {
        needRefresh = true;
        selectedUrl = URL;
    }

    boolean save(BufferedImage img) {
        if (currentUrl == null) return false;
        try {
            File output = new File(
                    OFFLINE_FILES,
                    currentUrl.replace("https://", "").replaceAll("['\"/\\\\<>|]", "-") + ".png"
            );
            if (output.exists()) return false;
            ImageIO.write(
                    img, "png", output
            );
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BufferedImage nextImage() {
        if (!loading && (!OFFLINE_MODE && (imagePaths.size() < 6 || needRefresh))) {
            loading = true;

            System.out.println("loading");
            Thread thread = new Thread(this::fetchImages);
            thread.start();
        }

        if (cachingImage) {
            try {
                System.out.println("reading image");
                cachedThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        BufferedImage image = cachedImage;
        cachingImage = true;
        cachedThread = new Thread(this::cacheNextImage);
        cachedThread.start();

        currentUrl = cachedUrl;
        return image;
    }

    private void cacheNextImage() {
        if (OFFLINE_MODE) {
            cachedImage  = nextOfflineFile();
            cachingImage = false;
            return;
        }

        try {
            if (!imagePaths.empty()){
                ImagePath imagePath = imagePaths.pop();
                cachedUrl = imagePath.link;

                BufferedImage image = ImageIO.read(imagePath.path);
                boolean       _     = imagePath.path.delete();
                cachedImage = image;
            } else {
                cachedUrl = null;
                cachedImage = nextOfflineFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cachingImage = false;
    }

    private BufferedImage nextOfflineFile() {
        try {
            if (offlineIndex == offlinePaths.length) offlineIndex = 0;
            return ImageIO.read(offlinePaths[offlineIndex++]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    record ImagePath(File path, String link) {}
}
