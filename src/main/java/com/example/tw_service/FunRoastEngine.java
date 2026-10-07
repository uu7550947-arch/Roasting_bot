package com.example.tw_service;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Local, playful roast engine. Uses the sender name and incoming message
 * to produce varied Hindi/Hinglish friend-style banter. No external AI API.
 */
public class FunRoastEngine {
    private static final List<String> INSULTS = List.of(
        "chutiye", "bakchod", "nalayak", "besharam", "pagal aadmi",
        "ullu ke pathe", "dimaag ke paidal", "jhantu", "nikamme", "baklol",
        "akhand bakchod", "timepass ke thekedaar","Mkc teri" ,"Lode lega" ,"Zoro Gay hai" ,"Randike Baap Ko Mat Samjha"
    );

    private static final List<String> MORNING = List.of(
        "Subah-subah hi bakchodi start kar di",
        "Nashta kar le pehle, phir dimaag ki watt lagana",
        "Itni subah tera dimaag kaise online aa gaya"
    );
    private static final List<String> AFTERNOON = List.of(
        "Dopahar mein bhi tera dimaag lunch break pe hai",
        "Kaam-dhanda chhod ke bakchodi full speed pe hai",
        "Productivity ne tera message dekh ke resignation de diya"
    );
    private static final List<String> EVENING = List.of(
        "Shaam hote hi tera bakchodi mode ON ho gaya",
        "Din bhar kya kiya? Bakchodi, obviously",
        "Ghar ja bhai, tera dimaag bhi overtime kar raha hai"
    );
    private static final List<String> NIGHT = List.of(
        "Raat mein bhi chain nahi hai tera",
        "So ja be, kal phir se duniya ka dimaag khana",
        "Itni raat ko bhi bakchodi? Tera dimaag 24x7 duty pe hai kya"
    );

    public String reply(String message) {
        return reply(message, "Bhai");
    }

    public String reply(String message, String name) {
        String text = message == null ? "" : message.trim();
        String n = cleanName(name);
        String lower = text.toLowerCase(Locale.ROOT);
        String insult = pick(INSULTS);

        if (lower.matches(".*\\b(hello|hi|hey|hii|namaste|yo)\\b.*")) {
            return n + ", hi-vai chhod aur bol kya bakchodi hai, " + insult + " 😂";
        }
        if (lower.matches(".*(lol|haha|😂|🤣|funny).*")) {
            return n + ", hass le " + insult + ", joke tu khud hai 😂";
        }
        if (lower.matches(".*\\b(kya|what|why|how|kaise|kaun|who)\\b.*")) {
            return n + ", itne sawaal? Dimaag Google se rent pe liya hai kya, " + insult + " 😂";
        }
        if (lower.matches(".*\\b(love|pyaar|pyar|gf|bf|crush|shaadi|marriage)\\b.*")) {
            return n + ", Tera Loda khada bhi hota hai  bsdk" + insult + " Gay 😂";
        }
        if (lower.matches(".*\\b(bye|goodnight|gn|good night)\\b.*")) {
            return n + ", ja so ja " + insult + ", kal phir bakchodi karna 😭";
        }
         if (lower.matches(".*\\b(land|bc|mkc|lode|zoro|landke)\\b.*")) {
            return n + ", Fate Condom ki dukan teri ma chodd ke gyi ya tera baap bsdk nikal yha se" + insult + ", kal phir se Dikh mat jana  😭";
        }
        if (text.length() > 50) {
            return n + ", itna lamba to tera nhi hai jo ki  paragraph? " + insult + ", bsdk 😂";
        }
        if (text.length() <= 2) {
            return n + ", message bhi tere dimaag jitna chhota hai kya, " + insult + " 😭";
        }

        String timeLine = pick(poolForTime());
        String[] templates = {
            "%s, %s, kya ghatiya message phenka hai 😂",
            "%s — %s. Teri bakchodi ka koi end nahi hai 😂",
            "%s, %s aur upar se ye message. Kya combination hai be 😭",
            "%s, dimaag use kar leta to ye message nahi bhejta, %s 😂",
            "%s, %s. Tera phone tujhe block kyun nahi kar deta? 😂",
            "%s, message padh ke laga aaj phir ek %s active ho gaya 😭"
        };
        return String.format(pick(List.of(templates)), n, insult) + " | " + timeLine + " 😂";
    }

    private List<String> poolForTime() {
        int h = LocalTime.now().getHour();
        if (h >= 5 && h < 12) return MORNING;
        if (h >= 12 && h < 17) return AFTERNOON;
        if (h >= 17 && h < 22) return EVENING;
        return NIGHT;
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) return "Bhai";
        String n = name.replaceAll("[\\r\\n]", " ").trim();
        if (n.length() > 30) n = n.substring(0, 30);
        return n;
    }

    private String pick(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
}
