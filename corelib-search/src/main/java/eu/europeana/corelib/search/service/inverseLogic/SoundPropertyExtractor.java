package eu.europeana.corelib.search.service.inverseLogic;

public class SoundPropertyExtractor {
    //public static final Integer mask = (1 << 25) - 1;

    public static String getQuality(Integer tag) {
        final Integer mask = 3 << 13;
        final Integer qualityCode = (tag & mask)>>13;

        if (1 == qualityCode) {
            return "true";
        }

        return "";
    }

    public static String getDuration(Integer tag) {
        final Integer mask = 7 << 10;
        final Integer durationCode = (tag & mask)>>10;

        switch (durationCode) {
            case 1: return "very_short";
            case 2: return "short";
            case 3: return "medium";
            case 4: return "long";

            default: return "";
        }
    }
}
