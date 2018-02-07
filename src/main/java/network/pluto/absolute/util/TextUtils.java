package network.pluto.absolute.util;

import java.text.Normalizer;

public class TextUtils {

    public static String normalize(String text) {
        String nfdNormalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String mnRemoved = nfdNormalized.replaceAll("\\p{Mn}", "");
        String nfcNormalized = Normalizer.normalize(mnRemoved, Normalizer.Form.NFC);
        String lowerCased = nfcNormalized.toLowerCase();
        String specialCharRemoved = lowerCased.replaceAll("[!\"\'#$%&()*+\\-,./:;<=>?@\\[\\\\\\]^_`{|}~“”]+", " ");
        String spaceRemoved = specialCharRemoved.replaceAll("[\\s\\p{Zs}]+", " ");
        return spaceRemoved.trim();
    }

}
