package com.zhm.springboot.userservice.blockchain.security;

import com.zhm.springboot.userservice.blockchain.config.WalletProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * 钱包凭证管理服务
 * 提供钱包创建、助记词生成、Keystore 管理等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCredentialService {

    private final WalletProperties walletProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    // BIP39 助记词词表
    private static final List<String> BIP39_WORDS = List.of(
        "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absurd", "abuse",
        "access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire", "across", "act",
        "action", "actor", "actress", "actual", "adapt", "add", "addict", "address", "adjust", "admit",
        "adult", "advance", "advice", "aerobic", "affair", "afford", "afraid", "again", "age", "agent",
        "agree", "ahead", "aim", "air", "airport", "aisle", "alarm", "album", "alcohol", "alert",
        "alien", "all", "alley", "allow", "almost", "alone", "alpha", "already", "also", "alter",
        "always", "amateur", "amazing", "among", "amount", "amused", "analyst", "anchor", "ancient", "anger",
        "angle", "angry", "animal", "ankle", "announce", "annual", "another", "answer", "antenna", "antique",
        "anxiety", "any", "apart", "apology", "appear", "apple", "approve", "april", "arch", "arctic",
        "area", "arena", "argue", "arm", "armed", "armor", "army", "around", "arrange", "arrest",
        "arrive", "arrow", "art", "artefact", "artist", "artwork", "ask", "aspect", "assault", "asset",
        "assist", "assume", "asthma", "athlete", "atom", "attack", "attend", "attitude", "attract", "auction",
        "audit", "august", "aunt", "author", "auto", "autumn", "average", "avocado", "avoid", "awake",
        "aware", "away", "awesome", "awful", "awkward", "axis", "baby", "bachelor", "bacon", "badge",
        "bag", "balance", "balcony", "ball", "bamboo", "banana", "banner", "bar", "barely", "bargain",
        "barrel", "base", "basic", "basket", "battle", "beach", "bean", "beauty", "because", "become",
        "beef", "before", "begin", "behave", "behind", "believe", "below", "belt", "bench", "benefit",
        "best", "betray", "better", "between", "beyond", "bicycle", "bid", "bike", "bind", "biology",
        "bird", "birth", "bitter", "black", "blade", "blame", "blanket", "blast", "bleak", "bless",
        "blind", "blood", "blossom", "blouse", "blue", "blur", "blush", "board", "boat", "body",
        "boil", "bomb", "bone", "bonus", "book", "boost", "border", "boring", "borrow", "boss",
        "bottom", "bounce", "box", "boy", "bracket", "brain", "brand", "brass", "brave", "bread",
        "breeze", "brick", "bridge", "brief", "bright", "bring", "brisk", "broccoli", "broken", "bronze",
        "broom", "brother", "brown", "brush", "bubble", "buddy", "budget", "buffalo", "build", "bulb",
        "bulk", "bullet", "bundle", "bunker", "burden", "burger", "burst", "bus", "business", "busy",
        "butter", "buyer", "buzz", "cabbage", "cabin", "cable", "cactus", "cage", "cake", "call",
        "calm", "camera", "camp", "can", "canal", "cancel", "candy", "cannon", "canoe", "canvas",
        "canyon", "capable", "capital", "captain", "car", "carbon", "card", "cargo", "carpet", "carry",
        "cart", "case", "cash", "casino", "castle", "casual", "cat", "catalog", "catch", "category",
        "cattle", "caught", "cause", "caution", "cave", "ceiling", "celery", "cement", "census", "century",
        "cereal", "certain", "chair", "chalk", "champion", "change", "chaos", "chapter", "charge", "chase",
        "chat", "cheap", "check", "cheese", "chef", "cherry", "chest", "chicken", "chief", "child",
        "chimney", "choice", "choose", "chronic", "chuckle", "chunk", "churn", "cigar", "cinnamon", "circle",
        "citizen", "city", "civil", "claim", "clap", "clarify", "claw", "clay", "clean", "clerk",
        "clever", "click", "client", "cliff", "climb", "clinic", "clip", "clock", "clog", "close",
        "cloth", "cloud", "clown", "club", "clump", "cluster", "clutch", "coach", "coast", "coconut",
        "code", "coffee", "coil", "coin", "collect", "color", "column", "combine", "come", "comfort",
        "comic", "common", "company", "concert", "conduct", "confirm", "congress", "connect", "consider", "control",
        "convince", "cook", "cool", "copper", "copy", "coral", "core", "corn", "corners", "correct",
        "cost", "cotton", "couch", "country", "couple", "course", "cousin", "cover", "coyote", "crack",
        "cradle", "craft", "cram", "crane", "crash", "crater", "crawl", "crazy", "cream", "credit",
        "creek", "crew", "cricket", "crime", "crisp", "critic", "crop", "cross", "crouch", "crowd",
        "crucial", "cruel", "cruise", "crumble", "crunch", "crush", "cry", "crystal", "cube", "culture",
        "cup", "cupboard", "curious", "current", "curtain", "curve", "cushion", "custom", "cute", "cycle",
        "dad", "damage", "damp", "dance", "danger", "daring", "dash", "daughter", "dawn", "day",
        "deal", "debate", "debris", "decade", "december", "decide", "decline", "decorate", "decrease", "deer",
        "defense", "define", "defy", "degree", "delay", "deliver", "demand", "demise", "denial", "dentist",
        "deny", "depart", "depend", "deposit", "depth", "deputy", "derive", "describe", "desert", "design",
        "desk", "despair", "destroy", "detail", "detect", "develop", "device", "devote", "diagram", "dial",
        "diamond", "diary", "dice", "diesel", "diet", "differ", "digital", "dignity", "dilemma", "dinner",
        "dinosaur", "direct", "dirt", "disagree", "discover", "disease", "dish", "dismiss", "disorder", "display",
        "distance", "divert", "divide", "divorce", "dizzy", "doctor", "document", "dog", "doll", "dolphin",
        "domain", "donate", "donkey", "donor", "door", "dose", "double", "dove", "draft", "dragon",
        "drama", "draw", "dream", "dress", "drift", "drill", "drink", "drip", "drive", "drop",
        "drum", "dry", "duck", "dumb", "dune", "during", "dust", "dutch", "duty", "dwarf",
        "dynamic", "eager", "eagle", "early", "earn", "earth", "ease", "east", "easy", "eat",
        "echo", "ecology", "economy", "edge", "edit", "educate", "effort", "egg", "eight", "either",
        "elbow", "elder", "electric", "elegant", "element", "elephant", "elevator", "elite", "else", "embark",
        "embody", "embrace", "emerge", "emotion", "employ", "empower", "empty", "enable", "enact", "end",
        "endless", "endorse", "enemy", "energy", "enforce", "engage", "engine", "enhance", "enjoy", "enlist",
        "enough", "enrich", "enroll", "ensure", "enter", "entire", "entry", "envelope", "episode", "equal",
        "equip", "era", "erase", "erode", "erosion", "error", "erupt", "escape", "essay", "essence",
        "estate", "eternal", "ethics", "evidence", "evil", "evoke", "evolve", "exact", "example", "excess",
        "exchange", "excite", "exclude", "excuse", "execute", "exercise", "exhaust", "exhibit", "exile", "exist",
        "exit", "exotic", "expand", "expect", "expire", "explain", "expose", "express", "extend", "extra",
        "eye", "eyebrow", "fabric", "face", "faculty", "fade", "faint", "faith", "fall", "false",
        "fame", "family", "famous", "fan", "fancy", "fantasy", "farm", "fashion", "fat", "fatal",
        "father", "fatigue", "fault", "favorite", "feature", "february", "federal", "fee", "feed", "feel",
        "female", "fence", "festival", "fetch", "fever", "few", "fiber", "fiction", "field", "figure",
        "file", "film", "filter", "final", "find", "fine", "finger", "finish", "fire", "firm",
        "first", "fiscal", "fish", "fit", "fitness", "fix", "flag", "flame", "flash", "flat",
        "flavor", "flee", "flight", "flip", "float", "flock", "floor", "flower", "fluid", "flush",
        "fly", "foam", "focus", "fog", "foil", "fold", "follow", "food", "foot", "force",
        "forest", "forget", "fork", "fortune", "forum", "forward", "fossil", "foster", "found", "fox",
        "fragile", "frame", "frequent", "fresh", "friend", "fringe", "frog", "front", "frost", "frown",
        "frozen", "fruit", "fuel", "fun", "funny", "furnace", "fury", "future", "gadget", "gain",
        "galaxy", "gallery", "game", "gap", "garage", "garbage", "garden", "garlic", "garment", "gas",
        "gasp", "gate", "gather", "gauge", "gaze", "general", "genius", "genre", "gentle", "genuine",
        "gesture", "ghost", "giant", "gift", "giggle", "ginger", "giraffe", "girl", "give", "glad",
        "glance", "glare", "glass", "glide", "glimpse", "globe", "gloom", "glory", "glove", "glow",
        "glue", "goat", "goddess", "gold", "good", "goose", "gorilla", "gospel", "gossip", "govern",
        "gown", "grab", "grace", "grain", "grant", "grape", "grass", "gravity", "great", "green",
        "grid", "grief", "grit", "grocery", "group", "grow", "grunt", "guard", "guess", "guide",
        "guilt", "guitar", "gun", "gym", "habit", "hair", "half", "hammer", "hamster", "hand",
        "handle", "harbor", "hard", "harsh", "harvest", "hat", "have", "hawk", "hazard", "head",
        "health", "heart", "heavy", "hedgehog", "height", "hello", "helmet", "help", "hen", "hero",
        "hidden", "high", "hill", "hint", "hip", "hire", "history", "hobby", "hockey", "hold",
        "hole", "holiday", "hollow", "home", "honey", "hood", "hope", "horn", "horror", "horse",
        "hospital", "host", "hotel", "hour", "hover", "hub", "huge", "human", "humble", "humor",
        "hundred", "hungry", "hunt", "hurdle", "hurry", "hurt", "husband", "hybrid", "ice", "icon",
        "idea", "identify", "idle", "ignore", "ill", "illegal", "illness", "image", "imitate", "immense",
        "immune", "impact", "impose", "improve", "impulse", "inch", "include", "income", "increase", "index",
        "indicate", "indoor", "industry", "infant", "inflict", "inform", "inhale", "inherit", "initial", "inject",
        "injury", "inmate", "inner", "innocent", "input", "inquiry", "insane", "insect", "inside", "inspire",
        "install", "intact", "interest", "into", "invest", "invite", "involve", "iron", "island", "isolate",
        "issue", "item", "ivory", "jacket", "jaguar", "jar", "jazz", "jealous", "jeans", "jelly",
        "jewel", "job", "join", "joke", "journey", "joy", "judge", "juice", "jump", "jungle",
        "junior", "junk", "just", "kangaroo", "keen", "keep", "ketchup", "key", "kick", "kid",
        "kidney", "kind", "kingdom", "kiss", "kit", "kitchen", "kite", "kitten", "kiwi", "knee",
        "knife", "knock", "know", "lab", "label", "labor", "ladder", "lady", "lake", "lamp",
        "language", "laptop", "large", "later", "latin", "laugh", "laundry", "lava", "law", "lawn",
        "lawsuit", "layer", "lazy", "leader", "leaf", "learn", "leave", "lecture", "left", "leg",
        "legal", "legend", "leisure", "lemon", "lend", "length", "lens", "leopard", "lesson", "letter",
        "level", "liar", "liberty", "library", "license", "life", "lift", "light", "like", "limb",
        "limit", "link", "lion", "liquid", "list", "little", "live", "lizard", "load", "loan",
        "lobster", "local", "lock", "logic", "lonely", "long", "loop", "lottery", "loud", "lounge",
        "love", "loyal", "lucky", "luggage", "lumber", "lunar", "lunch", "luxury", "lyrics", "machine",
        "mad", "magic", "magnet", "maid", "mail", "main", "major", "make", "mammal", "man",
        "manage", "mandate", "mango", "mansion", "manual", "maple", "marble", "march", "margin", "marine",
        "market", "marriage", "mask", "mass", "master", "match", "material", "math", "matrix", "matter",
        "maximum", "maze", "meadow", "mean", "measure", "meat", "mechanic", "medal", "media", "melody",
        "melt", "member", "memory", "mention", "menu", "mercy", "merge", "merit", "merry", "mesh",
        "message", "metal", "method", "middle", "midnight", "milk", "million", "mimic", "mind", "minimum",
        "minor", "minute", "miracle", "mirror", "misery", "miss", "mistake", "mix", "mixed", "mixture",
        "mobile", "model", "modify", "mom", "moment", "monitor", "monkey", "monster", "month", "moon",
        "moral", "more", "morning", "mosquito", "mother", "motion", "motor", "mountain", "mouse", "move",
        "movie", "much", "muffin", "mule", "multiply", "muscle", "museum", "mushroom", "music", "must",
        "mutual", "myself", "mystery", "myth", "naive", "name", "napkin", "narrow", "nasty", "nation",
        "nature", "near", "neck", "need", "negative", "neglect", "neither", "nephew", "nerve", "nest",
        "net", "network", "neutral", "never", "news", "next", "nice", "night", "noble", "noise",
        "nominee", "noodle", "normal", "north", "nose", "notable", "note", "nothing", "notice", "novel",
        "now", "nuclear", "number", "nurse", "nut", "oak", "obey", "object", "oblige", "obscure",
        "observe", "obtain", "obvious", "occur", "ocean", "october", "odor", "off", "offer", "office",
        "often", "oil", "okay", "old", "olive", "olympic", "omit", "once", "one", "onion",
        "online", "only", "open", "opera", "opinion", "oppose", "option", "orange", "orbit", "orchard",
        "order", "ordinary", "organ", "orient", "original", "orphan", "ostrich", "other", "outdoor", "outer",
        "output", "outside", "oval", "oven", "over", "own", "owner", "oxygen", "oyster", "ozone",
        "pact", "paddle", "page", "pair", "palace", "palm", "panda", "panel", "panic", "panther",
        "paper", "parade", "parent", "park", "parrot", "party", "pass", "patch", "path", "patient",
        "patrol", "pattern", "pause", "pave", "payment", "peace", "peanut", "pear", "peasant", "pelican",
        "pen", "penalty", "pencil", "people", "pepper", "perfect", "permit", "person", "pet", "phone",
        "photo", "phrase", "physical", "piano", "picnic", "picture", "piece", "pig", "pigeon", "pill",
        "pilot", "pink", "pioneer", "pipe", "pistol", "pitch", "pizza", "place", "planet", "plastic",
        "plate", "play", "please", "pledge", "pluck", "plug", "plunge", "poem", "poet", "point",
        "polar", "pole", "police", "pond", "pony", "pool", "popular", "portion", "position", "possible",
        "post", "potato", "pottery", "poverty", "powder", "power", "practice", "praise", "predict", "prefer",
        "prepare", "present", "pretty", "prevent", "price", "pride", "primary", "print", "priority", "prison",
        "private", "prize", "problem", "process", "produce", "profit", "program", "project", "promote", "proof",
        "property", "prosper", "protect", "proud", "provide", "public", "pudding", "pull", "pulp", "pulse",
        "pumpkin", "punch", "pupil", "puppy", "purchase", "purity", "purpose", "purse", "push", "put",
        "puzzle", "pyramid", "quality", "quantum", "quarter", "question", "quick", "quit", "quiz", "quote",
        "rabbit", "raccoon", "race", "rack", "radar", "radio", "rail", "rain", "raise", "rally",
        "ramp", "ranch", "random", "range", "rapid", "rare", "rate", "rather", "raven", "raw",
        "razor", "ready", "real", "reason", "rebel", "rebuild", "recall", "receive", "recipe", "record",
        "recycle", "reduce", "reflect", "reform", "refuse", "region", "regret", "regular", "reject", "relax",
        "release", "relief", "rely", "remain", "remember", "remind", "remove", "render", "renew", "rent",
        "reopen", "repair", "repeat", "replace", "report", "require", "rescue", "resemble", "resist", "resource",
        "response", "result", "retire", "retreat", "return", "reunion", "reveal", "review", "reward", "rhythm",
        "rib", "ribbon", "rice", "rich", "ride", "ridge", "rifle", "right", "rigid", "ring",
        "riot", "ripple", "risk", "ritual", "rival", "river", "road", "roast", "robot", "robust",
        "rocket", "romance", "roof", "rookie", "room", "rose", "rotate", "rough", "round", "route",
        "royal", "rubber", "rude", "rug", "rule", "run", "runway", "rural", "sad", "saddle",
        "sadness", "safe", "sail", "salad", "salmon", "salon", "salt", "salute", "same", "sample",
        "sand", "satisfy", "satoshi", "sauce", "sausage", "save", "say", "scale", "scan", "scare",
        "scatter", "scene", "scheme", "school", "science", "scissors", "scorpion", "scout", "scrap", "screen",
        "script", "scrub", "sea", "search", "season", "seat", "second", "secret", "section", "security",
        "seed", "seek", "segment", "select", "sell", "seminar", "senior", "sense", "sentence", "series",
        "service", "session", "settle", "setup", "seven", "shadow", "shaft", "shallow", "share", "shed",
        "shell", "sheriff", "shield", "shift", "shine", "ship", "shiver", "shock", "shoe", "shoot",
        "shop", "short", "shoulder", "shove", "shrimp", "shrug", "shuffle", "shy", "sibling", "sick",
        "side", "siege", "sight", "sign", "silent", "silk", "silly", "silver", "similar", "simple",
        "since", "sing", "siren", "sister", "sit", "situation", "six", "size", "skate", "sketch",
        "ski", "skill", "skin", "skirt", "skull", "slab", "slam", "sleep", "slender", "slice",
        "slide", "slight", "slim", "slogan", "slot", "slow", "slush", "small", "smart", "smile",
        "smoke", "smooth", "snack", "snake", "snap", "sniff", "snow", "soap", "soccer", "social",
        "sock", "soda", "soft", "solar", "soldier", "solid", "solution", "solve", "someone", "song",
        "soon", "sorry", "sort", "soul", "sound", "soup", "source", "south", "space", "spare",
        "spatial", "spawn", "speak", "special", "speed", "spell", "spend", "sphere", "spice", "spider",
        "spike", "spin", "spirit", "split", "spoil", "sponsor", "spoon", "sport", "spot", "spray",
        "spread", "spring", "spy", "square", "squeeze", "squirrel", "stable", "stadium", "staff", "stage",
        "stairs", "stamp", "stand", "start", "state", "stay", "steak", "steel", "stem", "step",
        "stereo", "stick", "still", "sting", "stock", "stomach", "stone", "stool", "story", "stove",
        "strategy", "street", "strike", "strong", "struggle", "student", "stuff", "stumble", "style", "subject",
        "submit", "subway", "success", "such", "sudden", "suffer", "sugar", "suggest", "suit", "summer",
        "sun", "sunny", "sunset", "super", "supply", "supreme", "sure", "surface", "surge", "surprise",
        "surround", "survey", "suspect", "sustain", "swallow", "swamp", "swap", "swarm", "swear", "sweet",
        "swift", "swim", "swing", "switch", "sword", "symbol", "symptom", "syrup", "system", "table",
        "tackle", "tag", "tail", "talent", "talk", "tank", "tape", "target", "task", "taste",
        "tattoo", "taxi", "teach", "team", "tell", "ten", "tenant", "tennis", "tent", "term",
        "test", "text", "thank", "that", "theme", "then", "theory", "there", "they", "thing",
        "this", "thought", "three", "thrive", "throw", "thumb", "thunder", "ticket", "tide", "tiger",
        "tilt", "timber", "time", "tiny", "tip", "tired", "tissue", "title", "toast", "tobacco",
        "today", "toddler", "toe", "together", "toilet", "token", "tomato", "tomorrow", "tone", "tongue",
        "tonight", "tool", "tooth", "top", "topic", "topple", "torch", "tornado", "tortoise", "toss",
        "total", "tourist", "toward", "tower", "town", "toy", "track", "trade", "traffic", "tragic",
        "train", "transfer", "trap", "trash", "travel", "tray", "treat", "tree", "trend", "trial",
        "tribe", "trick", "trigger", "trim", "trip", "trophy", "trouble", "truck", "true", "truly",
        "trumpet", "trust", "truth", "try", "tube", "tuition", "tumble", "tuna", "tunnel", "turkey",
        "turn", "turtle", "twelve", "twenty", "twice", "twin", "twist", "two", "type", "typical",
        "ugly", "umbrella", "unable", "unaware", "uncle", "uncover", "under", "undo", "unfair", "unfold",
        "unhappy", "uniform", "unique", "unit", "universe", "unknown", "unlock", "until", "unusual", "unveil",
        "update", "upgrade", "uphold", "upon", "upper", "upset", "urban", "urge", "usage", "use",
        "used", "useful", "useless", "usual", "utility", "vacant", "vacuum", "vague", "valid", "valley",
        "valve", "van", "vanish", "vapor", "various", "vegan", "velvet", "vendor", "venture", "venue",
        "verb", "verify", "version", "very", "vessel", "veteran", "viable", "vibrant", "vicious", "victory",
        "video", "view", "village", "vintage", "violin", "virtual", "virus", "visa", "visit", "visual",
        "vital", "vivid", "vocal", "voice", "void", "volcano", "volume", "vote", "voyage", "wage",
        "wagon", "wait", "walk", "wall", "walnut", "want", "warfare", "warm", "warrior", "wash",
        "wasp", "waste", "water", "wave", "way", "wealth", "weapon", "wear", "weasel", "weather",
        "web", "wedding", "weekend", "weird", "welcome", "west", "wet", "whale", "what", "wheat",
        "wheel", "when", "where", "whip", "whisper", "wide", "width", "wife", "wild", "will",
        "win", "window", "wine", "wing", "wink", "winner", "winter", "wire", "wisdom", "wise",
        "wish", "witness", "wolf", "woman", "wonder", "wood", "wool", "word", "work", "world",
        "worry", "worth", "wrap", "wreck", "wrestle", "wrist", "write", "wrong", "yard", "year",
        "yellow", "you", "young", "youth", "zebra", "zero", "zone", "zoo"
    );

    /**
     * 创建新钱包
     *
     * @param password 加密密码
     * @return 钱包凭证信息
     */
    public WalletCredentials createWallet(String password) {
        try {
            // 使用 Web3j 创建钱包（Web3j 4.12.2 的 API）
            // 先生成 ECKeyPair，然后创建 Credentials
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            Credentials credentials = Credentials.create(ecKeyPair);
            String address = credentials.getAddress();
            String privateKey = ecKeyPair.getPrivateKey().toString(16);

            // 创建 Keystore 文件
            File keystoreDir = new File(walletProperties.getKeystorePath());
            if (!keystoreDir.exists()) {
                keystoreDir.mkdirs();
            }
            String keystoreFileName = WalletUtils.generateFullNewWalletFile(password, keystoreDir);
            File keystoreFile = new File(keystoreDir, keystoreFileName);
            String keystorePath = keystoreFile.getAbsolutePath();

            // 读取 Keystore 内容
            String keystore = Files.readString(keystoreFile.toPath());

            // 生成助记词（简化版本，实际应使用 HDWallet）
            String mnemonic = generateMnemonic();

            log.info("Created new wallet: {}", address);

            return WalletCredentials.builder()
                    .address(address)
                    .mnemonic(mnemonic)
                    .privateKey(privateKey)
                    .keystore(keystore)
                    .keystorePath(keystorePath)
                    .credentials(credentials)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create wallet", e);
            throw new RuntimeException("Failed to create wallet: " + e.getMessage(), e);
        }
    }

    /**
     * 从助记词恢复钱包（简化版本）
     *
     * @param mnemonic 助记词
     * @return 私钥
     */
    public String restoreFromMnemonic(String mnemonic) {
        try {
            return generatePrivateKeyFromMnemonic(mnemonic);
        } catch (Exception e) {
            log.error("Failed to restore wallet from mnemonic", e);
            throw new RuntimeException("Failed to restore wallet: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Keystore 加载凭证（简化版本）
     *
     * @param keystorePath Keystore 文件路径
     * @param password     解密密码
     * @return 私钥
     */
    public String loadFromKeystore(String keystorePath, String password) {
        try {
            String keystore = Files.readString(Paths.get(keystorePath));
            // 简化版本：直接返回存储的私钥
            return decryptPrivateKey(keystore, password);
        } catch (Exception e) {
            log.error("Failed to load credentials from keystore: {}", keystorePath, e);
            throw new RuntimeException("Failed to load credentials: " + e.getMessage(), e);
        }
    }

    /**
     * 从私钥加载凭证
     *
     * @param privateKey 私钥（16 进制字符串）
     * @return 私钥
     */
    public String loadFromPrivateKey(String privateKey) {
        return privateKey;
    }

    /**
     * 生成助记词
     *
     * @return 12 词助记词
     */
    public String generateMnemonic() {
        StringBuilder mnemonic = new StringBuilder();
        Random random = new Random(secureRandom.nextInt());
        for (int i = 0; i < 12; i++) {
            if (i > 0) mnemonic.append(" ");
            mnemonic.append(BIP39_WORDS.get(random.nextInt(BIP39_WORDS.size())));
        }
        return mnemonic.toString();
    }

    /**
     * 验证助记词是否有效（简化版本）
     *
     * @param mnemonic 助记词
     * @return 是否有效
     */
    public boolean validateMnemonic(String mnemonic) {
        if (mnemonic == null || mnemonic.isEmpty()) {
            return false;
        }
        String[] words = mnemonic.trim().split("\\s+");
        if (words.length != 12) {
            return false;
        }
        for (String word : words) {
            if (!BIP39_WORDS.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从助记词生成地址（简化版本）
     */
    private String generateAddressFromMnemonic(String mnemonic) {
        try {
            // 使用 SHA256 生成地址
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(mnemonic.getBytes("UTF-8"), "address".getBytes("UTF-8"), 2048);
            KeyParameter key = (KeyParameter) gen.generateDerivedParameters(256);

            byte[] keyBytes = key.getKey();
            StringBuilder address = new StringBuilder("0x");
            for (int i = 12; i < keyBytes.length; i++) {
                address.append(String.format("%02x", keyBytes[i]));
            }
            return address.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate address", e);
        }
    }

    /**
     * 从助记词生成私钥（简化版本）
     */
    private String generatePrivateKeyFromMnemonic(String mnemonic) {
        try {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(mnemonic.getBytes("UTF-8"), "".getBytes("UTF-8"), 2048);
            KeyParameter key = (KeyParameter) gen.generateDerivedParameters(256);

            byte[] keyBytes = key.getKey();
            StringBuilder privateKey = new StringBuilder();
            for (byte b : keyBytes) {
                privateKey.append(String.format("%02x", b));
            }
            return privateKey.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate private key", e);
        }
    }

    /**
     * 创建简单的 Keystore（简化版本）
     */
    private String createSimpleKeystore(String address, String password) {
        return "{\"address\":\"" + address + "\",\"crypto\":{\"encrypted\":true}}";
    }

    /**
     * 保存 Keystore 到文件
     */
    private String saveKeystore(String keystore, String address) throws IOException {
        Path keystoreDir = Paths.get(walletProperties.getKeystorePath());
        if (!Files.exists(keystoreDir)) {
            Files.createDirectories(keystoreDir);
        }

        String fileName = String.format("UTC--%s--%s",
                System.currentTimeMillis() / 1000,
                address.toLowerCase());

        Path keystorePath = keystoreDir.resolve(fileName);
        Files.writeString(keystorePath, keystore);

        log.info("Saved keystore to: {}", keystorePath);
        return keystorePath.toString();
    }

    /**
     * 加密私钥
     */
    public String encryptPrivateKey(String privateKey, String password) {
        try {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(password.getBytes("UTF-8"), password.substring(0, 8).getBytes("UTF-8"), 20000);
            KeyParameter key = (KeyParameter) gen.generateDerivedParameters(256);

            byte[] keyBytes = key.getKey();
            byte[] privateKeyBytes = privateKey.getBytes("UTF-8");

            byte[] encrypted = new byte[privateKeyBytes.length];
            for (int i = 0; i < privateKeyBytes.length; i++) {
                encrypted[i] = (byte) (privateKeyBytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Failed to encrypt private key", e);
            throw new RuntimeException("Failed to encrypt private key", e);
        }
    }

    /**
     * 解密私钥
     */
    public String decryptPrivateKey(String encryptedPrivateKey, String password) {
        try {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(password.getBytes("UTF-8"), password.substring(0, 8).getBytes("UTF-8"), 20000);
            KeyParameter key = (KeyParameter) gen.generateDerivedParameters(256);

            byte[] keyBytes = key.getKey();
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPrivateKey);

            byte[] decrypted = new byte[encryptedBytes.length];
            for (int i = 0; i < encryptedBytes.length; i++) {
                decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            log.error("Failed to decrypt private key", e);
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }

    // Hardhat/Foundry 默认公开测试私钥，禁止用于生产环境
    private static final String HARDHAT_KNOWN_KEY =
            "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

    /**
     * 获取平台钱包凭证
     */
    public String getPlatformPrivateKey() {
        String privateKey = walletProperties.getPlatformPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException(
                "平台私钥未配置，请设置环境变量 BLOCKCHAIN_PLATFORM_PRIVATE_KEY");
        }
        if (HARDHAT_KNOWN_KEY.equalsIgnoreCase(privateKey.trim())) {
            throw new IllegalStateException(
                "检测到 Hardhat 公开测试私钥，该私钥任何人均可控制对应账户，禁止用于生产环境。" +
                "请通过环境变量 BLOCKCHAIN_PLATFORM_PRIVATE_KEY 设置真实私钥");
        }
        return privateKey;
    }

    /**
     * 钱包凭证数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class WalletCredentials {
        /**
         * 钱包地址
         */
        private String address;

        /**
         * 助记词
         */
        private String mnemonic;

        /**
         * 私钥（16 进制字符串）
         */
        private String privateKey;

        /**
         * Keystore JSON 字符串
         */
        private String keystore;

        /**
         * Keystore 文件路径
         */
        private String keystorePath;

        /**
         * Web3j Credentials（可选）
         */
        @lombok.Builder.Default
        private Credentials credentials = null;
    }
}