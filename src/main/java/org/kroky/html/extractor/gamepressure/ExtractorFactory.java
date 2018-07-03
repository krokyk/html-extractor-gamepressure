package org.kroky.html.extractor.gamepressure;

public final class ExtractorFactory {

    private static final ExtractorFactory instance = new ExtractorFactory();

    private ExtractorFactory() {
    }

    public static ExtractorFactory getInstance() {
        return instance;
    }

    public Extractor newExtractor(String url) {
        if (url.contains("https://guides.gamepressure.com/masseffect3/")) {
            return new ME3Extractor();
        } else {
            throw new RuntimeException(String.format("Extractor not implemented for URL: %s", url));
        }
    }
}
