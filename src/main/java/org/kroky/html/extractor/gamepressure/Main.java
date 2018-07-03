package org.kroky.html.extractor.gamepressure;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            throw new RuntimeException(
                    "Program needs a URL as an argument. E.g. \"java -jar html-extractor-gamepressure-1.0.jar https://guides.gamepressure.com/masseffect3/\"");
        }
        for (String url : args) {
            Extractor extractor = ExtractorFactory.getInstance().newExtractor(url);
            extractor.extract(url);
        }
    }
}
