package org.kroky.html.extractor.gamepressure;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class ME3Extractor extends DefaultExtractor {

    @Override
    protected void postProcess(Element extractRoot) {
        extractRoot.attr("style", "font-size: 8pt;text-align: justify");
        extractRoot.select("style").forEach(styleElement -> styleElement.remove());

        Elements h2Elements = extractRoot.select("h2");
        h2Elements.forEach(h2 -> h2.after("<br>"));
        h2Elements.forEach(h2 -> h2.tagName("b").attr("style", "font-size: 16pt;text-align: left").appendElement("br"));

        // remove <center> if it does not contain any element
        extractRoot.select("center").stream().filter(el -> el.children().isEmpty())
                .forEach(el -> removeElement(el, false, true));

        // remove all Walkthrough texts
        extractRoot.select("b").stream().filter(el -> el.text().contains("Walkthrough"))
                .forEach(el -> removeElement(el, false, true));

        // remove Map Legend
        extractRoot.select("span.gb12gold").stream().forEach(el -> removeElement(el, false, true));

        // put map legend on one line (remove <br> and replace it with " &nbsp;")
        Element legendElement = extractRoot.selectFirst("span.gb10");
        if (legendElement != null) {
            legendElement.select("br").forEach(br -> br.replaceWith(new TextNode(" " + DefaultExtractor.NBSP)));
            String legendText = extractRoot.selectFirst("span.gb10").wholeText();
            extractRoot.selectFirst("table.gbtable")
                    .replaceWith(new Element("center").attr("style", "font-size: 6pt").text(legendText));
        }

        // remove <br> from <li> elements
        extractRoot.select("li").forEach(li -> li.select("br").forEach(br -> br.remove()));

        // remove <br> before <ul> elements
        extractRoot.select("ul").forEach(ul -> trimLeadingBr(ul));
    }

}
