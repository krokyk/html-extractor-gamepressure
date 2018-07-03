package org.kroky.html.extractor.gamepressure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public abstract class DefaultExtractor implements Extractor {
    private static final Logger LOG = LogManager.getFormatterLogger(DefaultExtractor.class);

    protected static final String TITLE_SELECTOR = "div.tcat_tl";
    protected static final String CONTENT_SELECTOR = "div.gb13";
    protected static final String DID_WE_MISS_ANYTHING_SELECTOR = "span.gb10";
    protected static final String NBSP = "\u00a0";

    @Override
    public void extract(String url) throws IOException {
        List<String> contentLinks = getContentLinks(url);

        List<String> extracts = contentLinks.stream().map(href -> getContentFromLink(href))
                .collect(Collectors.toList());
        Files.write(Paths.get(getTargetFilename(url)), extracts);
    }

    protected String getTargetFilename(String url) {
        return url.replaceAll("https?://", "").replaceAll("[\\\\/:\\*\\?\"<>\\|]", "-") + ".html";
    }

    protected List<String> getContentLinks(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element rightcolumn = doc.getElementById("rightcolumn");
        return rightcolumn.select("a").stream().map(a -> a.attr("abs:href")).collect(Collectors.toList());
    }

    protected void urlToAbsolute(Element root, String... elementsWithUrlAttr) {
        Arrays.stream(elementsWithUrlAttr).forEach(elName -> root.select(elName).forEach(el -> {
            el.attr("src", el.attr("abs:src"));
            el.attr("href", el.attr("abs:href"));
        }));
    }

    protected void removeAdSection(Element root) {
        // everything between <!-- begin 300 x 250 advertisement --> ... <!-- end 300 x 250 advertisement -->

        Node adCommentNode = root.childNodes().stream().filter(node -> "#comment".equals(node.nodeName())
                && node.outerHtml().contains("begin 300 x 250 advertisement")).findFirst().orElse(null);
        if (adCommentNode != null) {
            Node sibling = adCommentNode.nextSibling();
            while (sibling != null && !sibling.outerHtml().contains("end 300 x 250 advertisement")) {
                sibling.remove();
                sibling = adCommentNode.nextSibling();
            }
        }
    }

    // trim means remove leading and trailing BR
    protected void removeElements(List<Element> elements, boolean trimLeading, boolean trimTrailing) {
        elements.forEach(element -> {
            removeElement(element, trimLeading, trimTrailing);
        });

    }

    protected void removeElement(Element element, boolean trimLeading, boolean trimTrailing) {
        if (element == null) {
            return;
        }
        if (trimTrailing) {
            trimTrailingBr(element);
        }
        if (trimLeading) {
            trimLeadingBr(element);
        }
        element.remove();
    }

    protected void trimLeadingBr(Element element) {
        Element sibling;
        while ((sibling = element.previousElementSibling()) != null && sibling.tagName().equalsIgnoreCase("br")) {
            sibling.remove();
        }
    }

    protected void trimTrailingBr(Element element) {
        Element sibling;
        while ((sibling = element.nextElementSibling()) != null && sibling.tagName().equalsIgnoreCase("br")) {
            sibling.remove();
        }
    }

    protected String getContentFromLink(String url) {
        Document doc;
        LOG.info("Processing URL: %s", url);
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("<h1>!!! Could not get contents from %s !!!<h1>", url);
        }

        preProcess(doc);

        Element titleElement = getTitleElement(doc);
        Element extractRoot = getExtractRoot(doc);
        List<Element> didWeMissAnythingElements = getDidWeMissAnythingElements(extractRoot);
        removeElements(didWeMissAnythingElements, true, true);

        // remove advertisement section
        removeAdSection(extractRoot);

        // prepend the title to the very beginning of this extract
        extractRoot.childNodes().get(0).before(titleElement);

        // make all urls absolute (i.e. http://<domain_name>/...)
        urlToAbsolute(extractRoot, "img", "a");

        postProcess(extractRoot); // if some implementation needs to add something, override this method

        return extractRoot.toString();
    }

    protected void postProcess(Element extractRoot) {
        // default implementation is empty
    }

    protected void preProcess(Document doc) {
        // default implementation is empty
    }

    protected Element getTitleElement(Document doc) {
        return doc.selectFirst(TITLE_SELECTOR).tagName("h2");
    }

    protected Element getExtractRoot(Document doc) {
        return doc.selectFirst(CONTENT_SELECTOR);
    }

    protected List<Element> getDidWeMissAnythingElements(Element extractRoot) {
        Elements els = extractRoot.select(DID_WE_MISS_ANYTHING_SELECTOR);

        return els.stream().filter(el -> el.text().contains("Did we miss anything in this section?"))
                .collect(Collectors.toList());
    }
}
